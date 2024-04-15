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
import sys
import pprint
from datasets import load_dataset
from collections import Counter 

def go_back(directory):
	return os.path.abspath(os.path.join(directory, '..'))

cwd = os.getcwd()
cwd = go_back(cwd)
cwd += '/yaml_conversion'
sys.path.insert(0, cwd)
import yaml_converter
cwd = go_back(cwd)
cwd += '/final_model'


model_name = "NousResearch/Llama-2-7b-hf"

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
# model.config.use_cache = False
# model.config.pretraining_tp = 1

# # Load LLaMA tokenizer
# tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
# tokenizer.pad_token = tokenizer.eos_token
# tokenizer.padding_side = "right" # Fix weird overflow issue with fp16 training

# # Load adapter weights; this is a simplified example assuming the state dict matches.
# adapter_weights = torch.load("pretrained_models/Llama-2-7b-finetune/adapter_model.bin")
# model.load_state_dict(adapter_weights, strict=False)

# Load the pre-trained model and tokenizer
# model = AutoModelWithHeads.from_pretrained(model_name)
tokenizer = AutoTokenizer.from_pretrained(model_name)

# Load your custom adapter. Replace 'path_to_adapter' with the actual path to your adapter
adapter_path = "pretrained_models/Llama-2-7b-finetune"
adapter_name = model.load_adapter(adapter_path)

# Activate the adapter
model.active_adapters = adapter_name

prompt = input("Enter your question: ")
# print(prompt)
# pipe = pipeline(task="text-generation", model=model, tokenizer=tokenizer, max_length=200)
# prompt = f"<s>[INST] {prompt} [/INST] </s>"
# #result = pipe(f"<s>[INST] {prompt} [/INST]")
# #generated_text = result[0]['generated_text']
# input_ids = tokenizer.encode(prompt, return_tensors="pt").to(model.device)
# output_sequences = model.generate(input_ids, max_length=200) 
# generated_text = tokenizer.decode(output_sequences[0], skip_special_tokens=True)
# Ignore warnings
logging.set_verbosity(logging.CRITICAL)

# Run text generation pipeline with our next model
# prompt = "Create a CanNm module with a GlobalConfig container. In the CanNmGlobalConfig container, create 24 CanNmChannelConfig containers. In each, set the value of CanNmMsgReducedTime to 6.256 and create 20 CanNmRxPdu containers. In each, set the value of CanNmRxPduId to 54079."
pipe = pipeline(task="text-generation", model=model, tokenizer=tokenizer, max_length=500)
result = pipe(f"<s>[INST] {prompt} [/INST]")
# with open("llama_output.yaml", "w") as f:
#     f.write(result[0]['generated_text'])
print("")
print("")
print(result[0]['generated_text'])
print("")
print("")

# # Prepare the input text, which might need to be tokenized
# input_ids = tokenizer.encode(prompt, return_tensors="pt").to(model.device)

# # Generate an output sequence from the input
# output_sequences = model.generate(input_ids, max_length=200) 

# # Decode the output sequences to text
# generated_text = tokenizer.decode(output_sequences[0], skip_special_tokens=True)

# Now, save the file inside the subdirectory
generated_file = "generated_yaml.yml"
cwd = os.getcwd()
cwd = go_back(cwd)
cwd += '/yaml_conversion'
generated_file_path = cwd + "/" + generated_file

with open(generated_file_path, "w") as f:
    f.write(result[0]['generated_text'])

# Change the file path to inside the "yaml_conversion" directory
sys.path.insert(0, cwd)
yaml_converter.convert(generated_file, "generated_arxml.arxml")
