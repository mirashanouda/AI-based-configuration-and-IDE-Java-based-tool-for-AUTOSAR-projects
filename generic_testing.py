import xml.etree.ElementTree as ET
from lxml import etree
import random
import items
import parsing
import base_code
import gen_func

yaml = ""
english = ""
dataset_name = "testing_dataset.jsonl"

beginning_english = ""
beginning_yaml = ""
beginning = "{\"text\": \"<s>[INST] "

beginning_english += "Create a CanNm module with a GlobalConfig container. "
beginning_yaml += "[/INST] "
beginning_yaml += "\\nModule:"
beginning_yaml += "\\n\\tName: CanNm"
beginning_yaml += "\\n\\tContainer:\\n"
beginning_yaml += base_code.bslashts(2) + "- Name: CanNmGlobalConfig\\n"

# ------------------------------ Case 1 ------------------------------ 
with open(dataset_name, "w") as file:
  num_files = 1000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = gen_func.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
    yaml += listt[0]
    english += listt[1]

    listt = gen_func.create_exact_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1, 1)
    yaml += listt[0]
    english += listt[1]
    
    listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmRxPdu"]), 3, max_rand)
    yaml += listt[0]
    english += listt[1]

    listt = gen_func.create_exact_parameters(int(base_code.container_dict["CanNmRxPdu"]), 4, 0, 1)
    yaml += listt[0]
    english += listt[1]
       
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# -------------------------------------------------------------------- 
    

# # ------------------------------ Case 2 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 2000 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1, 5)
#     yaml += listt[0]
#     english += listt[1]
    
#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmRxPdu"]), 3, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmRxPdu"]), 4, 0, 5)
#     yaml += listt[0]
#     english += listt[1]
       
#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # -------------------------------------------------------------------- 
    

# # ------------------------------ Case 3 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 500 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 0, 1)
#     yaml += listt[0]
#     english += listt[1]
       
#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # -------------------------------------------------------------------- 
    


# # ------------------------------ Case 4 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 500 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 0, 5)
#     yaml += listt[0]
#     english += listt[1]
       
#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # --------------------------------------------------------------------
    


# # ------------------------------ Case 5 ------------------------------
# with open(dataset_name, "w") as file:
#   num_files = 500 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1, 1)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_single_subcontainer(int(base_code.container_dict["CanNmTxPdu"]), 3)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmTxPdu"]), 4, 0, 1)
#     yaml += listt[0]
#     english += listt[1]
       
#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # --------------------------------------------------------------------


# # ------------------------------ Case 6 ------------------------------
# with open(dataset_name, "w") as file:
#   num_files = 500 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1, 1)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_single_subcontainer(int(base_code.container_dict["CanNmUserDataTxPdu"]), 3)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmUserDataTxPdu"]), 4, 0, 1)
#     yaml += listt[0]
#     english += listt[1]
       
#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # -------------------------------------------------------------------- 
    



# # ------------------------------ Case 11 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 500 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_single_subcontainer(int(base_code.container_dict["CanNmChannelConfig"]), 2)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 0, 5)
#     yaml += listt[0]
#     english += listt[1]
       
#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # --------------------------------------------------------------------
    

# # ------------------------------ Case 7 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 1000 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_exact_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1, 1)
#     yaml += listt[0]
#     english += listt[1]
    
#     num_sets = random.randint(1, 4)
#     for _ in range(num_sets):
#       listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmRxPdu"]), 3, max_rand)
#       yaml += listt[0]
#       english += listt[1]
       
#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # -------------------------------------------------------------------- 



# # ------------------------------ Case 8 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 1000 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1, 1)
#     yaml += listt[0]
#     english += listt[1]
    
#     num_sets = random.randint(1, 4)
#     for _ in range(num_sets):
#       listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmRxPdu"]), 3, max_rand)
#       yaml += listt[0]
#       english += listt[1]
       
#       listt = gen_func.create_exact_parameters(int(base_code.container_dict["CanNmRxPdu"]), 4, 0, 1)
#       yaml += listt[0]
#       english += listt[1]

#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # --------------------------------------------------------------------


# # ------------------------------ Case 12 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 1000 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     num_setss = random.randint(1, 2)

#     for _ in range(num_setss):
#       listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#       yaml += listt[0]
#       english += listt[1]

#       listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1, 2)
#       yaml += listt[0]
#       english += listt[1]
      
#       num_sets = random.randint(1, 4)
#       for _ in range(num_sets):
#         listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmRxPdu"]), 3, max_rand)
#         yaml += listt[0]
#         english += listt[1]
          
#         listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmRxPdu"]), 4, 0, 2)
#         yaml += listt[0]
#         english += listt[1]

#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # -------------------------------------------------------------------- 
    

# # ------------------------------ Case 9 ------------------------------ 
# with open(dataset_name, "w") as file:
#   num_files = 1000 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]
    
#     num_sets = random.randint(1, 4)
#     for _ in range(num_sets):
#       listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmChannelConfig"]), 2, max_rand)
#       yaml += listt[0]
#       english += listt[1]

#       listt = gen_func.create_exact_parameters(int(base_code.container_dict["CanNmChannelConfig"]), 3, 0, 1)
#       yaml += listt[0]
#       english += listt[1]

#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # --------------------------------------------------------------------
    


# # ------------------------------ Case 10 ----------------------------- 
# with open(dataset_name, "w") as file:
#   num_files = 1000 # number of input/output pairs to generate
#   max_rand = 25 # maximum number of subcontainers inside containers
#   for _ in range(num_files):

#     english = ""
#     yaml = ""

#     english += beginning_english
#     yaml += beginning_yaml

#     listt = gen_func.generate_global_parameter()
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmPnInfo"]), 2, max_rand)
#     yaml += listt[0]
#     english += listt[1]

#     listt = gen_func.create_random_parameters(int(base_code.container_dict["CanNmPnInfo"]), 3, 1, 1)
#     yaml += listt[0]
#     english += listt[1]
    
#     num_sets = random.randint(1, 4)
#     for _ in range(num_sets):
#       listt = gen_func.create_subcontainers(int(base_code.container_dict["CanNmPnFilterMaskByte"]), 3, max_rand)
#       yaml += listt[0]
#       english += listt[1]
       
#       listt = gen_func.create_exact_parameters(int(base_code.container_dict["CanNmPnFilterMaskByte"]), 4, 0, 1)
#       yaml += listt[0]
#       english += listt[1]

#     file.write(beginning)
#     file.write(english)
#     file.write(yaml)
#     file.write("</s>\"}\n")
# # --------------------------------------------------------------------