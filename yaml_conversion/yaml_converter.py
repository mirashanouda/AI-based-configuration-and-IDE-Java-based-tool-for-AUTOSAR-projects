import xml.etree.ElementTree as ET
from lxml import etree
import yaml
import os
import sys
import yaml_utility
import yaml_validator
import tabs_to_spaces_converter

def go_back(directory):
	return os.path.abspath(os.path.join(directory, '..'))

cwd = os.getcwd()
cwd = go_back(cwd)
cwd += '/global_dependencies'
sys.path.insert(0, cwd)
import items
import et_init
import tag_file
import parameter_file
import container_file
import global_vars
cwd = go_back(cwd)
cwd += '/parsing'
sys.path.insert(0, cwd)
import parsing
cwd = go_back(cwd)
cwd += '/yaml_conversion'

# Reseting the global variables
global_vars.et_elements = []
AS = et_init.initialize(global_vars.et_elements)

def dfs_convert(data, parent_dr):
    c = container_file.container()
    name = yaml_utility.retrieve_single(data, 'Name')
    c.short_name.set_text(name)
    container_idx = global_vars.container_dict[name]
    c.set_in_tag_value(["UUID", global_vars.container_def[container_idx].UUID])
    c.parent_def_ref.text = parent_dr
    c.declare_container()
    
    # Add parameters and default parameters    
    # Actual parameters:
    parameter_list = yaml_utility.retrieve_list(data, 'Parameter')
    parameter_dict = {}
    for param in parameter_list:
        p = parameter_file.parameter()
        p.def_name = str(param['Name'])
        for param2 in global_vars.container_parameters[container_idx]:
            if str(param2.def_name) == str(p.def_name):
                p.param_type = param2.data_type
                p.name = 'ECUC-' + str(param2.decl_name) + '-PARAM-VALUE'                    
                break
        
        parameter_dict[str(param['Name'])] = 1
        p.value.set_text(str(param['Value']))
        p.parent_def_ref = c.def_ref
        p.declare_parameter()
        c.add_parameter(p)
        
    # Default parameters:
    for param in global_vars.container_parameters[container_idx]:
        if str(param.def_name) not in parameter_dict:
            for _ in range(int(param.lower_mult)):
                p = parameter_file.parameter()
                p.def_name = param.def_name
                p.name = 'ECUC-' + str(param.decl_name) + '-PARAM-VALUE'
                p.parent_def_ref = c.def_ref
                p.param_type = param.data_type
                p.value.set_text(str(parsing.get_default_value(param.data_type)))
                p.declare_parameter()
                c.add_parameter(p)
    
    children_list = yaml_utility.retrieve_list(data, 'Container')
    for child in children_list:
        for i in range(int(yaml_utility.retrieve_single(child, 'Multiplicity'))):
            c.add_child(dfs_convert(child, c.parent_def_ref.text + '/' + c.short_name.text))
    return c


def is_valid_yaml(filename):
    try:
        with open(filename, 'r') as file:
            yaml.safe_load(file)  # Attempt to load the YAML data
        return True  # Return True if parsing is successful
    except yaml.YAMLError:  # Catch any YAML parsing errors
        return False  # Return False if an error occurs

def convert(input_yaml, output_xml):
    cwd = os.getcwd()
    cwd = go_back(cwd)
    cwd += '/yaml_conversion'
    input_yaml_path = cwd + '/' + input_yaml
    output_yaml_path = cwd + '/output.yml'
    tabs_to_spaces_converter.convert_tabs_to_spaces(input_yaml_path, output_yaml_path)
    if not is_valid_yaml(output_yaml_path):
        with open("error.txt", "w") as file:
            file.write("Failure!")
        return
    with open(output_yaml_path, 'r') as file:
        data = yaml.safe_load(file)
        
    if not yaml_validator.validate(data):
        with open("error.txt", "w") as file:
            file.write("Failure!")
        return
    
    c = container_file.container()
    c.short_name.set_text('CanNmGlobalConfig') # Hardcoded
    c.set_in_tag_value(["UUID", "e40d0e8a-816a-426a-9159-e229afbced6d"]) # Hardcoded
    c.parent_def_ref.text = "/AUTOSAR/EcucDefs/CanNm" # Hardcoded
    c.declare_container()
        
    data = data['Module']
    data = data['Container']
    
    # Add parameters and default parameters    
    # Actual parameters:
    parameter_list = yaml_utility.retrieve_list(data, 'Parameter')
    parameter_dict = {}
    container_idx = global_vars.container_dict['CanNmGlobalConfig']
    for param in parameter_list:
        p = parameter_file.parameter()
        p.def_name = str(param['Name'])
        for param2 in global_vars.container_parameters[container_idx]:
            if str(param2.def_name) == str(p.def_name):
                p.param_type = param2.data_type
                p.name = 'ECUC-' + str(param2.decl_name) + '-PARAM-VALUE'                    
                break
        
        parameter_dict[str(param['Name'])] = 1
        p.value.set_text(str(param['Value']))
        p.parent_def_ref = c.def_ref
        p.declare_parameter()
        c.add_parameter(p)
        
    # Default parameters:
    for param in global_vars.container_parameters[container_idx]:
        if str(param.def_name) not in parameter_dict:
            for _ in range(int(param.lower_mult)):
                p = parameter_file.parameter()
                p.def_name = param.def_name
                p.name = 'ECUC-' + str(param.decl_name) + '-PARAM-VALUE'
                p.parent_def_ref = c.def_ref
                p.param_type = param.data_type
                p.value.set_text(str(parsing.get_default_value(param.data_type)))
                p.declare_parameter()
                c.add_parameter(p)
    
    children_list = yaml_utility.retrieve_list(data, 'Container')
    for child in children_list:
        for i in range(int(yaml_utility.retrieve_single(child, 'Multiplicity'))):
            c.add_child(dfs_convert(child, c.parent_def_ref.text + '/' + c.short_name.text))
    global_vars.et_elements[0].append(global_vars.et_elements[c.et_element_idx])
    
    et_init.final_out(output_xml, AS)
    
    with open("error.txt", "w") as file:
        file.write("Success!")