import os, torch
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    BitsAndBytesConfig,
    HfArgumentParser,
    TrainingArguments,
    pipeline,
    logging,
)
import pprint
from datasets import load_dataset

# model_name = "NousResearch/CodeLlama-7b-hf"

# Compute dtype for 4-bit base models
bnb_4bit_compute_dtype = "float16"

# Activate 4-bit precision base model loading
use_4bit = True

# Compute dtype for 4-bit base models
bnb_4bit_compute_dtype = "float16"

# Quantization type (fp4 or nf4)
bnb_4bit_quant_type = "nf4"

# Activate nested quantization for 4-bit base models (double quantization)
use_nested_quant = False

compute_dtype = getattr(torch, bnb_4bit_compute_dtype)

# Load the entire model on the GPU 0
device_map = {"": 0}

# Check GPU compatibility with bfloat16
if compute_dtype == torch.float16 and use_4bit:
    major, _ = torch.cuda.get_device_capability()
    if major >= 8:
        print("=" * 80)
        print("Your GPU supports bfloat16: accelerate training with bf16=True")
        print("=" * 80)

bnb_config = BitsAndBytesConfig(
    load_in_4bit=use_4bit,
    bnb_4bit_quant_type=bnb_4bit_quant_type,
    bnb_4bit_compute_dtype=compute_dtype,
    bnb_4bit_use_double_quant=use_nested_quant,
)

# Load base model
model = AutoModelForCausalLM.from_pretrained(
    model_name,
    quantization_config=bnb_config,
    device_map=device_map
)
model.config.use_cache = False
model.config.pretraining_tp = 1

# Load LLaMA tokenizer
tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
tokenizer.pad_token = tokenizer.eos_token
tokenizer.padding_side = "right" # Fix weird overflow issue with fp16 training

# Load adapter weights; this is a simplified example assuming the state dict matches.
adapter_weights = torch.load("pretrained_models/CodeLlama-2-7b-finetune/adapter_model.bin")
model.load_state_dict(adapter_weights, strict=False)

# Example test prompt
dataset_name = "Proofyy/testing_dataset"
# Load dataset (you can process it here)
dataset = load_dataset(dataset_name, split="test")

# Define the directories
base_dir = "output"
sub_dir = "code_llama_output"

# Create the base direpprintctory if it doesn't exist
if not os.path.exists(base_dir):
    os.makedirs(base_dir)

# Create the subdirectory inside the base directory if it doesn't exist
file_num = 1
full_path = os.path.join(base_dir, sub_dir)
if not os.path.exists(full_path):
    os.makedirs(full_path)

for example in dataset:
    # print("Example:")
    # pprint.pprint(example)
    # Prepare the input text, which might need to be tokenized
    input_ids = tokenizer.encode(example["text"], return_tensors="pt").to(model.device)
    
    # Generate an output sequence from the input
    output_sequences = model.generate(input_ids, max_length=200) 
    
    # Decode the output sequences to text
    generated_text = tokenizer.decode(output_sequences[0], skip_special_tokens=True)

    # Now, save the file inside the subdirectory
    generated_file_path = os.path.join(full_path, f"generated_{file_num}.txt")
    sample_file_path = os.path.join(full_path, f"sample_{file_num}.txt")
    file_num += 1

    with open(generated_file_path, "w") as f:
        f.write(generated_text)

    with open(sample_file_path, "w") as f:
        f.write(example["text"])


# Calculating the acuracy of the model
def list_file_pairs(directory):
    generated_files = {}
    sample_files = {}
    
    # List all files in the directory
    for file_name in os.listdir(directory):
        if file_name.startswith('generated_'):
            file_number = file_name[len('generated_'):].split('.')[0]
            generated_files[file_number] = file_name
        elif file_name.startswith('sample_'):
            file_number = file_name[len('sample_'):].split('.')[0]
            sample_files[file_number] = file_name
            
    # Identify pairs based on file numbers
    file_pairs = [(generated_files[num], sample_files[num]) for num in generated_files if num in sample_files]
    return file_pairs

def read_file_content(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        return file.read()

def extract_relevant_section(content, x):
    """Extract the section of the content between 'Module:' and '</s>'."""
    start_marker = "Module:"
    end_marker = "</s>"
    start_index = content.find(start_marker)
    end_index = content.find(end_marker, start_index) + len(end_marker)
    if (x == 0):
        if start_index != -1:
            return content[start_index:]
        else:
            return None  # Or an empty string, depending on how you want to handle it
    else:
        if start_index != -1 and end_index != -1:
            return content[start_index:end_index - 4]
        else:
            return None  # Or an empty string, depending on how you want to handle it

def compare_files(file1_path, file2_path):
    """Compare the relevant sections of two files."""
    content1 = read_file_content(file1_path)
    content2 = read_file_content(file2_path)
    section1 = extract_relevant_section(content1, 0)
    section2 = extract_relevant_section(content2, 1)
    if (section1 != section2):
        print("#" * 80)
        print(section1)
        print("-" * 80)
        print(section2)
        print("#" * 80)
    
    return section1 == section2

directory = 'output/code_llama_output'  # Adjust directory as needed

file_pairs = list_file_pairs(directory)
success = 0
tot_files = 0
failed_files = []

for generated_file, sample_file in file_pairs:
    tot_files += 1
    are_equal = compare_files(os.path.join(directory, generated_file),
                              os.path.join(directory, sample_file))
    if are_equal:
        success += 1
    else:
        failed_files.append(tot_files)

if tot_files != 0:
    print(f"The Accuracy of the model is: {success / tot_files * 100:.2f}%")
print(f"Failed files: {failed_files}")