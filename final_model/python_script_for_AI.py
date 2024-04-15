import os
os.chdir(os.path.dirname(os.path.realpath(__file__)))
# Define the file paths
input_file_path = 'user_input.txt'
output_file_path = 'IT_WORKED.txt'

# Open and read the input file
with open(input_file_path, 'r') as input_file:
    # Read all lines in the file
    lines = input_file.readlines()
    with open(output_file_path, 'w') as output_file:
       output_file.write(lines[0])
       output_file.write("\n")
       output_file.write(os.getcwd())
        
