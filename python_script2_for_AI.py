import sys

def write_to_file(input_string):
    # This function writes the input string to a text file
    with open("OutputAI.txt", "w") as file:
        file.write(input_string)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        input_string = sys.argv[1]  # Get the string passed as an argument
        write_to_file(input_string)
    else:
        print("Please provide a string as an input argument.")
