import yaml
import os
import sys
import yaml_utility
import yaml_validator
import tabs_to_spaces_converter

yaml_file_name = "input.yml"
tabs_to_spaces_converter.convert_tabs_to_spaces(yaml_file_name, 'output.yml')
with open('output.yml', 'r') as file:
    data = yaml.safe_load(file)

print(yaml_validator.validate(data))