import os
import sys
import yaml
os.chdir(os.path.dirname(os.path.realpath(__file__)))
# sys.path.insert(0, os.path.dirname(os.path.realpath(__file__)))

def go_back(directory):
	return os.path.abspath(os.path.join(directory, '..'))

def is_valid_yaml(filename):
    try:
        with open(filename, 'r') as file:
            yaml.safe_load(file)  # Attempt to load the YAML data
        return True  # Return True if parsing is successful
    except yaml.YAMLError:  # Catch any YAML parsing errors
        return False  # Return False if an error occurs
    
def read_file_with_formatting(filename):
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    lines = []
    with open(filename, 'r') as file: 
        for line in file:
            lines.append(line)
    return lines

def reformat_string(word):
    return word.replace("\t", "    ").replace("\n", "")

def validate_yaml(line_list):
    with open("for_validation.yml", "w") as f:
        for line in line_list:
            f.write(line)
            f.write("\n")
    return is_valid_yaml("for_validation.yml")

def extract_yaml(filename, output_filename):
    line_list = read_file_with_formatting(filename)
    start = 0
    counter = 0
    for line in line_list:
        if line == "Module:\n":
            start = counter
            break
        counter += 1
    
    res_list = []
    for i in range(start, len(line_list)):
        res_list.append(reformat_string(line_list[i]))
    
    while not validate_yaml(res_list):
        res_list.pop()
    
    with open(output_filename, "w") as f:
        for line in res_list:
            f.write(line)
            f.write("\n")