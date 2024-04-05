
############################################# Import All the Required Libraries #############################################

import os
import torch
from datasets import load_dataset
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    BitsAndBytesConfig,
    HfArgumentParser,
    TrainingArguments,
    pipeline,
    logging,
)
from peft import LoraConfig, PeftModel
from trl import SFTTrainer
from sklearn.model_selection import KFold
import pprint

############################################# Choose the model name and dataset name #############################################

model_name = "NousResearch/Llama-2-7b-hf"
dataset_name = "Proofyy/hundred_dataset"
new_model = "Llama-2-7b-finetune"

############################################# Setting the fine-tuning parameters #############################################

# LoRA attention dimension
lora_r = 64

# Alpha parameter for LoRA scaling
lora_alpha = 16

# Dropout probability for LoRA layers
lora_dropout = 0.1

# Activate 4-bit precision base model loading
use_4bit = True

# Compute dtype for 4-bit base models
bnb_4bit_compute_dtype = "float16"

# Quantization type (fp4 or nf4)
bnb_4bit_quant_type = "nf4"

# Activate nested quantization for 4-bit base models (double quantization)
use_nested_quant = False

# Output directory where the model predictions and checkpoints will be stored
output_dir = "./results_llama"

# Number of training epochs
num_train_epochs = 1

# Enable fp16/bf16 training (set bf16 to True with an A100)
fp16 = False
bf16 = False

# Batch size per GPU for training
per_device_train_batch_size = 4

# Batch size per GPU for evaluation
per_device_eval_batch_size = 4

# Number of update steps to accumulate the gradients for
gradient_accumulation_steps = 1

# Enable gradient checkpointing
gradient_checkpointing = True

# Maximum gradient normal (gradient clipping)
max_grad_norm = 0.3

# Initial learning rate (AdamW optimizer)
learning_rate = 2e-4

# Weight decay to apply to all layers except bias/LayerNorm weights
weight_decay = 0.001

# Optimizer to use
optim = "paged_adamw_32bit"

# Learning rate schedule
lr_scheduler_type = "cosine"

# Number of training steps (overrides num_train_epochs)
max_steps = -1

# Ratio of steps for a linear warmup (from 0 to learning rate)
warmup_ratio = 0.03

# Saves memory and speeds up training considerably
group_by_length = True

# Save checkpoint every X updates steps
save_steps = 0

# Log every X updates steps
logging_steps = 25

# Maximum sequence length to use
max_seq_length = None

# Pack multiple short examples in the same input sequence to increase efficiency
packing = False

# Load the entire model on the GPU 0
device_map = {"": 0}

#############################################  Load everything and start the fine-tuning process #############################################

# Load dataset (you can process it here)
dataset = load_dataset(dataset_name)

# Load tokenizer and model with QLoRA configuration
compute_dtype = getattr(torch, bnb_4bit_compute_dtype)
bnb_config = BitsAndBytesConfig(
    load_in_4bit=use_4bit,
    bnb_4bit_quant_type=bnb_4bit_quant_type,
    bnb_4bit_compute_dtype=compute_dtype,
    bnb_4bit_use_double_quant=use_nested_quant,
)

# Check GPU compatibility with bfloat16
if compute_dtype == torch.float16 and use_4bit:
    major, _ = torch.cuda.get_device_capability()
    if major >= 8:
        print("=" * 80)
        print("Your GPU supports bfloat16: accelerate training with bf16=True")
        print("=" * 80)

# Specify the number of splits for cross-validation
num_splits = 4  # For example, 4-fold cross-validation

# Initialize the KFold mechanism
kf = KFold(n_splits=num_splits, shuffle=True, random_state=42)

# Initialize KFold cross-validation
file_num = 1
fold = 0
results = []

for train_index, val_index in kf.split(dataset['train']):
    fold += 1
    print(f"Starting fold {fold}")

    # Splitting the dataset into the training set and the validation set
    train_dataset = dataset['train'].select(train_index)
    val_dataset = dataset['train'].select(val_index)
    
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

    # Load LoRA configuration
    peft_config = LoraConfig(
        lora_alpha=lora_alpha,
        lora_dropout=lora_dropout,
        r=lora_r,
        bias="none",
        task_type="CAUSAL_LM",
    )

    # Set training parameters
    training_arguments = TrainingArguments(
        output_dir=output_dir,
        num_train_epochs=num_train_epochs,
        per_device_train_batch_size=per_device_train_batch_size,
        gradient_accumulation_steps=gradient_accumulation_steps,
        optim=optim,
        save_steps=save_steps,
        logging_steps=logging_steps,
        learning_rate=learning_rate,
        weight_decay=weight_decay,
        fp16=fp16,
        bf16=bf16,
        max_grad_norm=max_grad_norm,
        max_steps=max_steps,
        warmup_ratio=warmup_ratio,
        group_by_length=group_by_length,
        lr_scheduler_type=lr_scheduler_type,
        report_to="tensorboard"
    )

    # Set supervised fine-tuning parameters
    trainer = SFTTrainer(
        model=model,
        train_dataset=train_dataset,
        eval_dataset=val_dataset,
        peft_config=peft_config,
        dataset_text_field="text",
        max_seq_length=max_seq_length,
        tokenizer=tokenizer,
        args=training_arguments,
        packing=packing,
    )

    # Train model
    trainer.train()
    
    # Evaluate model on this fold, store or print results
    # eval_results = trainer.evaluate()
    
    # Define the directories
    base_dir = "pretrained_models"
    sub_dir = new_model

    # Create the base directory if it doesn't exist
    if not os.path.exists(base_dir):
        os.makedirs(base_dir)

    # Create the subdirectory inside the base directory if it doesn't exist
    full_path = os.path.join(base_dir, sub_dir)
    if not os.path.exists(full_path):
        os.makedirs(full_path)
    # Save the model for each fold if needed
    os.path.join(full_path, f"fold_{fold}.txt")
    trainer.model.save_pretrained(full_path)

    # Store or process your results from eval_results
    # results.append(eval_results)
    
    # Define the directories
    base_dir = "output"
    sub_dir = "llama_output"

    # Create the base direpprintctory if it doesn't exist
    if not os.path.exists(base_dir):
        os.makedirs(base_dir)

    # Create the subdirectory inside the base directory if it doesn't exist
    full_path = os.path.join(base_dir, sub_dir)
    if not os.path.exists(full_path):
        os.makedirs(full_path)
    # print("#" * 80)
    for example in val_dataset:
        # pprint.pprint(example)
        # Prepare the input text, which might need to be tokenized
        input_ids = tokenizer.encode(example["text"], return_tensors="pt").to(model.device)
        
        # Generate an output sequence from the input
        output_sequences = model.generate(input_ids, max_length=2000) 
        
        # Decode the output sequences to text
        generated_text = tokenizer.decode(output_sequences[0], skip_special_tokens=True)

        # Now, save the file inside the subdirectory
        generated_file_path = os.path.join(full_path, f"generated_{file_num}.txt")
        sample_file_path = os.path.join(full_path, f"sample_{file_num}.txt")
        file_num += 1

        with open(generated_file_path, "a") as f:
            f.write(generated_text)

        with open(sample_file_path, "a") as f:
            f.write(example["text"])

#############################################  Testing the model #############################################

# # Ignore warnings
# logging.set_verbosity(logging.CRITICAL)

# # Run text generation pipeline with our next model
# prompt = "Create a CanNm module with a GlobalConfig container. In the CanNmGlobalConfig container, create 24 CanNmChannelConfig containers. In each, set the value of CanNmMsgReducedTime to 6.256 and create 20 CanNmRxPdu containers. In each, set the value of CanNmRxPduId to 54079."
# pipe = pipeline(task="text-generation", model=model, tokenizer=tokenizer, max_length=400)
# result = pipe(f"<s>[INST] {prompt} [/INST]")
# with open("llama_output.yaml", "w") as f:
#     f.write(result[0]['generated_text'])