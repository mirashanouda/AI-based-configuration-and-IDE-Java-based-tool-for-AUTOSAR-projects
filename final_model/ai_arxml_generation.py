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
sys.path.insert(0, cwd)


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

tokenizer = AutoTokenizer.from_pretrained(model_name)

# Load your custom adapter. Replace 'path_to_adapter' with the actual path to your adapter
os.chdir(os.path.dirname(os.path.realpath(__file__)))
adapter_path = "pretrained_models/Llama-2-7b-finetune"
adapter_name = model.load_adapter(adapter_path)

# Activate the adapter
model.active_adapters = adapter_name

os.chdir(os.path.dirname(os.path.realpath(__file__)))
with open("user_input.txt", "r") as f:
    prompt = f.read()

# Ignore warnings
logging.set_verbosity(logging.CRITICAL)

# Run text generation pipeline with our next model
# prompt = "Create a CanNm module with a GlobalConfig container. In the CanNmGlobalConfig container, create 24 CanNmChannelConfig containers. In each, set the value of CanNmMsgReducedTime to 6.256 and create 20 CanNmRxPdu containers. In each, set the value of CanNmRxPduId to 54079."
pipe = pipeline(task="text-generation", model=model, tokenizer=tokenizer, max_length=500)
result = pipe(f"<s>[INST] {prompt} [/INST]")

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
yaml_converter.extract_and_convert(generated_file, "generated_arxml.arxml")