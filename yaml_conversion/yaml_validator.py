import yaml
import sys
import os
import yaml_utility

def go_back(directory):
	return os.path.abspath(os.path.join(directory, '..'))

cwd = os.getcwd()
cwd = go_back(cwd)
cwd += '/global_dependencies'
sys.path.insert(0, cwd)
import global_vars
cwd = go_back(cwd)
cwd += '/parsing'
sys.path.insert(0, cwd)
import parsing

cwd = go_back(cwd)
cwd += '/yaml_conversion'

def dfs(data, par_name):
    # check if I'm truly the son of my parent
    if yaml_utility.count(data, 'Name') != 1:
        return False
    
    name = yaml_utility.retrieve_single(data, 'Name')
    # Check if this is a valid name
    if name not in global_vars.container_dict:
        return False
    container_idx = global_vars.container_dict[name]
    if global_vars.par[container_idx] != -1:    
        if par_name != global_vars.container_def[global_vars.par[container_idx]].name:
            return False
    # I'm truly the son of my parent!
    
    # Now, check if my parameters are truly my parameters
    parameters = yaml_utility.retrieve_list(data, 'Parameter')
    for param in parameters:
        found = False
        for param2 in global_vars.container_parameters[container_idx]:
            if str(param2.def_name) == str(param['Name']):
                found = True
                break
        if not found:
            return False
    
    if name != "CanNmGlobalConfig":
        if yaml_utility.count(data, 'Multiplicity') != 1:
            return False
        
    res = True
    container_children = yaml_utility.retrieve_list(data, 'Container')
    for child in container_children:
        res = res and dfs(child, name)
    return res

def validate(data):
    if 'Module' not in data:
        return False
    data = data['Module']
    if 'Name' not in data:
        return False
    
    module_name = data['Name']
    if module_name != "CanNm":
        return False
    if 'Container' not in data:
        return False
    
    data = data['Container']
    if yaml_utility.count(data, 'Name') != 1:
        return False
    
    container_name = yaml_utility.retrieve_single(data, 'Name')
    if container_name != "CanNmGlobalConfig":
        return False    
    
    return dfs(data, "CanNmGlobalConfig")