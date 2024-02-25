import xml.etree.ElementTree as ET
from lxml import etree
import random

def initialize(et_elements):
  AUTOSAR = ET.Element("AUTOSAR")
  AUTOSAR.set("xmlns", "http://a...content-available-to-author-only...r.org/schema/r4.0")
  AUTOSAR.set("xmlns:xsi", "http://w...content-available-to-author-only...3.org/2001/XMLSchema-instance")
  AUTOSAR.set("xsi:schemaLocation", "http://a...content-available-to-author-only...r.org/schema/r4.0 AUTOSAR_00046.xsd")

  AR_PACKAGES = ET.Element("AR-PACKAGES")
  AR_PACKAGE = ET.Element("AR-PACKAGE")

  AUTOSAR.append(AR_PACKAGES)
  AR_PACKAGES.append(AR_PACKAGE)

  AUTOSAR_CONF = ET.SubElement(AR_PACKAGE, "SHORT-NAME")
  AUTOSAR_CONF.text = "AutosarConfigurator"

  ELEMENTS = ET.Element("ELEMENTS")
  AR_PACKAGE.append(ELEMENTS)

  ECUC_MODULE_CONFIGURATION_VALUES = ET.Element("ECUC-MODULE-CONFIGURATION-VALUES")
  ECUC_MODULE_CONFIGURATION_VALUES.set("UUID", "88315b75-2e55-4e07-8a67-e958b2d4694f")
  ELEMENTS.append(ECUC_MODULE_CONFIGURATION_VALUES)

  CanNm = ET.SubElement(ECUC_MODULE_CONFIGURATION_VALUES, "SHORT-NAME")
  CanNm.text = "CanNm"

  DEFINITION_REF = ET.SubElement(ECUC_MODULE_CONFIGURATION_VALUES, "DEFINITION-REF")
  DEFINITION_REF.text = "/AUTOSAR/EcucDefs/CanNm"
  DEFINITION_REF.set("DEST", "ECUC-MODULE-DEF")

  IMPLEMENTATION_CONFIG_VARIANT = ET.SubElement(ECUC_MODULE_CONFIGURATION_VALUES, "IMPLEMENTATION-CONFIG-VARIANT")
  IMPLEMENTATION_CONFIG_VARIANT.text = "VARIANT-POST-BUILD"

  MODULE_DESCRIPTION_REF = ET.SubElement(ECUC_MODULE_CONFIGURATION_VALUES, "MODULE_DESCRIPTION_REF")
  MODULE_DESCRIPTION_REF.text = "/AUTOSAR/EcucDefs/CanNm"
  MODULE_DESCRIPTION_REF.set("DEST", "BSW-IMPLEMENTATION")

  et_elements.append(ET.Element("CONTAINERS"))
  ECUC_MODULE_CONFIGURATION_VALUES.append(et_elements[0])

  return AUTOSAR

def final_out(file_name, et_ele):
  #et_ele = AUTOSAR
  tree = ET.ElementTree(et_ele)

  short_tab = "\t".expandtabs(2)
  ET.indent(tree, space=short_tab, level=0)
  with open (file_name, "wb") as files:
    tree.write(files)

class tag:
    name = ""
    text = ""
    is_sub = -1  # 0 -> element, 1 -> subelement
    in_tag_value = ["", ""]
    et_element_idx = -1  # the index of the et_element in the global array
    par = -1  # the parent index to be used in the case of subelement

    def __init__(self):
      name = ""

    def declare(self): # mostly if definition-ref or value
        # Assumes all attributes are set
        if self.is_sub == 0:
            et_elements.append(ET.Element(self.name))
            self.et_element_idx = len(et_elements) - 1

            if self.text != "":
                et_elements[self.et_element_idx].text = self.text

            if self.in_tag_value[0] != "":
                et_elements[self.et_element_idx].set(self.in_tag_value[0], self.in_tag_value[1])

        else:
            et_subelements.append(ET.SubElement(et_elements[self.par], self.name))
            self.et_element_idx = len(et_subelements) - 1

            if self.text != "":
                et_subelements[self.et_element_idx].text = self.text

            if self.in_tag_value[0] != "":
                et_subelements[self.et_element_idx].set(self.in_tag_value[0], self.in_tag_value[1])

    def get_name(self):
        return self.name

    def set_name(self, value):
        self.name = value

    def get_text(self):
        return self.text

    def set_text(self, value):
        self.text = value

    def get_is_sub(self):
        return self.is_sub

    def set_is_sub(self, value):
        self.is_sub = value

    def get_in_tag_value(self):
        return self.in_tag_value

    def set_in_tag_value(self, value):
        self.in_tag_value = value

    def get_et_element_idx(self):
        return self.et_element_idx

    def set_et_element_idx(self, value):
        self.et_element_idx = value

    def get_par(self):
        return self.par

    def set_par(self, value):
        self.par = value

class parameter(tag):

    def __init__(self):
        self.def_name = "" # the actual parameter name
        self.def_ref = tag()
        self.value = tag()
        self.param_type = ""  # float or boolean string
        self.par_cont = -1 # index of parent in the array of containers
        self.parent_def_ref = tag()

    def declare_parameter(self):  # adds the parameter to the file
        # assumes par_cont, param_type, def_name, value.text and self.name is set
        self.is_sub = 0
        super().declare()

        self.def_ref.set_name("DEFINTION-REF")
        self.def_ref.set_par(self.et_element_idx)
        def_ref_in_tag_value = "ECUC-" + self.param_type + "-PARAM-DEF"
        self.def_ref.set_in_tag_value(["DEST", def_ref_in_tag_value])

        # now do the dfs that makes the string of definition ref
        temp_text = self.parent_def_ref.text
        temp_text += "/" + self.def_name

        self.def_ref.set_text(temp_text)
        self.def_ref.set_is_sub(1)
        self.def_ref.declare()

        self.value.set_name("VALUE")
        self.value.set_is_sub(1)
        self.value.set_par(self.et_element_idx)
        self.value.declare()

    def get_def_name(self):
        return self.def_name

    def set_def_name(self, value):
        self.def_name = value

    def get_param_value(self):
        return self.param_value

    def set_param_value(self, value):
        self.param_value = value

    def get_par_cont(self):
        return self.par_cont

    def set_par_cont(self, value):
        self.par_cont = value

class container(tag):

  def __init__(self):
      self.short_name = tag()
      self.def_ref = tag()
      self.container_idx = -1
      self.par_cont = -1 # parent as a container
      self.sub_cont_idx = -1
      self.param_val_idx = -1
      self.has_param_val = 0
      self.has_sub_cont = 0
      self.parent_def_ref = tag()


  def declare_container(self):
    # assumes short_name.text is set
    # assumes children and parameters are added
    # (where parameters have their dependencies set except for par_cont and not declared)
    # (and children have their dependencies set except for par_cont and not declared)
    # assumes in_tag_value are given (the set value is UUID)
    # assumes par_cont and container_idx are given
    # assumes parent_def_ref is given to the container
    self.is_sub = 0
    self.name = "ECUC-CONTAINER-VALUE"
    self.declare()

    # create the short_name
    self.short_name.set_name("SHORT-NAME")
    self.short_name.set_is_sub(1)
    self.short_name.set_par(self.et_element_idx)
    self.short_name.declare()

    # create the def-ref
    self.def_ref.set_name("DEFINITION-REF")
    self.def_ref.set_in_tag_value(["DEST", "ECUC-PARAM-CONF-CONTAINER-DEF"])
    self.def_ref.set_is_sub(1)
    self.def_ref.set_par(self.et_element_idx)

    temp_text = self.parent_def_ref.text
    temp_text += "/" + self.short_name.text

    self.def_ref.set_text(temp_text)
    self.def_ref.declare()

  def add_parameter(self, param):
    if self.has_param_val == 0:
      self.has_param_val = 1
      self.param_val_idx = len(et_elements)
      et_elements.append(ET.Element("PARAMETER-VALUES"))
      et_elements[self.et_element_idx].append(et_elements[self.param_val_idx])
    et_elements[self.param_val_idx].append(et_elements[param.et_element_idx])

  def add_child(self, child):
    if self.has_sub_cont == 0:
      self.has_sub_cont = 1
      self.sub_cont_idx = len(et_elements)
      et_elements.append(ET.Element("SUB-CONTAINERS"))
      et_elements[self.et_element_idx].append(et_elements[self.sub_cont_idx])
    et_elements[self.sub_cont_idx].append(et_elements[child.et_element_idx])

  def get_par_cont(self):
    return self.par_cont

  def set_par_cont(self, value):
    self.par_cont = value

  def get_container_idx(self):
    return self.container_idx

  def set_container_idx(self, value):
    self.container_idx = value

class parameter_item:
  def __init__(self, def_name, value, data_type, decl_name, is_default, start_range, end_range, lower_mult, upper_mult):
    self.def_name = def_name
    self.value = value
    self.data_type = data_type
    self.decl_name = decl_name
    self.is_default = is_default
    self.start_range = start_range
    self.end_range = end_range
    self.lower_mult = lower_mult
    self.upper_mult = upper_mult

class container_item:
  def __init__(self, name, UUID, lower_mult, upper_mult):
    self.name = name
    self.UUID = UUID
    self.lower_mult = lower_mult
    self.upper_mult = upper_mult

max_nodes = 100000
container_def = []
par = [-1] * max_nodes
children = [[] for _ in range(max_nodes)]
container_parameters = [[] for _ in range(max_nodes)]

file_path = 'CanNM_BSWMD.arxml'

tree = etree.parse(file_path)
root = tree.getroot()

def process_container(ecuc_container):
  name = ecuc_container.find('./SHORT-NAME').text
  LM = ecuc_container.find('./LOWER-MULTIPLICITY').text
  UM = ecuc_container.find('./UPPER-MULTIPLICITY-INFINITE')
  if UM is not None:
    UM = float('inf')
  else:
    UM = ecuc_container.find('./UPPER-MULTIPLICITY').text
  UUID = ecuc_container.get('UUID')
  c = container_item(name, UUID, LM, UM)
  return c

def process_parameter(ecuc_parameter, typ):
  def_name = ecuc_parameter.find('./SHORT-NAME').text
  data_type = typ
  decl_name = 'TEXTUAL' if typ == 'ENUMERATION' else 'NUMERICAL'

  is_default = ecuc_parameter.find('./DEFAULT-VALUE')
  if is_default is not None:
    is_default = 1
    value = ecuc_parameter.find('./DEFAULT-VALUE').text
  else:
    is_default = 0
    value = -1

  if data_type == 'INTEGER':
    start_range = ecuc_parameter.find('./MIN').text
    end_range = ecuc_parameter.find('./MAX').text
  elif data_type == 'FLOAT':
    start_range = ecuc_parameter.find('./MIN')
    if start_range is not None:
      start_range = start_range.text
    else:
      start_range = -1
    end_range = ecuc_parameter.find('./MAX')
    if end_range is not None:
      end_range = end_range.text
    else:
      start_range = -1
  else:
    start_range = -1
    end_range = -1

  LM = ecuc_parameter.find('./LOWER-MULTIPLICITY').text
  UM = ecuc_parameter.find('./UPPER-MULTIPLICITY-INFINITE')
  if UM is not None:
    UM = float('inf')
  else:
    UM = ecuc_parameter.find('./UPPER-MULTIPLICITY').text
  print(def_name, value, data_type, decl_name, is_default, start_range, end_range, LM, UM)
  p = parameter_item(def_name, value, data_type, decl_name, is_default, start_range, end_range, LM, UM)
  return p

def get_parameters(params, idx):
  for param in params.findall('./ECUC-INTEGER-PARAM-DEF'):
    p = process_parameter(param, 'INTEGER')
    container_parameters[idx].append(p)
  for param in params.findall('./ECUC-FLOAT-PARAM-DEF'):
    p = process_parameter(param, 'FLOAT')
    container_parameters[idx].append(p)
  for param in params.findall('./ECUC-BOOLEAN-PARAM-DEF'):
    p = process_parameter(param, 'BOOLEAN')
    container_parameters[idx].append(p)
  for param in params.findall('./ECUC-ENUMERATION-PARAM-DEF'):
    p = process_parameter(param, 'ENUMERATION')
    container_parameters[idx].append(p)

def dfs (ecuc_container, level, p):
  for ecuc_container in ecuc_container.findall('./ECUC-PARAM-CONF-CONTAINER-DEF'):
    c = process_container(ecuc_container)
    container_def.append(c)
    idx = len(container_def) - 1
    par[idx] = p
    for params in ecuc_container.findall('./PARAMETERS'):
      get_parameters(params, idx)
    for sub_cont in ecuc_container.findall('./SUB-CONTAINERS'):
      dfs(sub_cont, level + 1, idx)

containers = root.findall('./CONTAINERS')[0]
dfs(containers, 0, -1)

et_elements = []
et_subelements = []
AS = initialize(et_elements)
children = [[] for _ in range(max_nodes)]

for i in range(7):
    if par[i] != -1:
        children[par[i]].append(i)

enumer_value = ['CANNM_PDU_BYTE_0', 'CANNM_PDU_BYTE_1', 'CANMN_PDU_OFF']
def get_default_value(partype):
  if partype == 'INTEGER':
    return 0
  elif partype == 'FLOAT':
    return 0
  elif partype == 'BOOLEAN':
    return "False"
  elif partype == 'ENUMERATION':
    return enumer_value[0]
  

class Set:
  def __init__(self):
    self.num_containers = 1
    self.subsets = [] # children sets
    self.parse_index = 0
    self.parameters = [] # param items in the set, according to the parsing, but the values changed
    self.param_default = [] # just to see whether we did the parameter or not
    self.par_set = -1
    

num_files = 10
max_num_containers = 100
max_num_subsets = 5
small_parameters_mode = 1

"""
Cases:
1. Lower 0 upper n -> <MULTIPLE> or empty
2. Lower 1 upper n -> <MULTIPLE> or <DEFAULT>
3. Lower 0 upper 1 -> <SINGLE> or empty
4. Lower 1 upper 1 -> <DEFAULT>
"""

for i in range(num_files):
  # generate the sets first and then generate the file
  sets = [Set()]

  label_name = "sets" + str(i + 1) + "_label" + ".txt"
  lout = open(label_name, "a")
  lout.write("Create a CanNm module;" + "\n")

  def dfs(node, depth):
    k = sets[node].parse_index
    d = depth
    while d > 0:
      lout.write("-")
      d -= 1
    if depth > 0:
      lout.write(" ")
    
    have_subedits = 0

    for child in children[k]:
      up = 0
      if str(container_def[child].upper_mult) == "1":
        up = 1
      else:
        up = max_num_containers
      low = int(container_def[child].lower_mult)
      # if up > 1:
      num_subsets = random.randint(low, max_num_subsets)
      for z in range(num_subsets):
        sets[node].subsets.append(len(sets))
        sets.append(Set())
        sets[len(sets) - 1].parse_index = child
        sets[len(sets) - 1].par_set = node
        sets[len(sets) - 1].num_containers = random.randint(low, up)
        have_subedits = 1    


    for param in container_parameters[k]:
      # append to the parameters of the set
      res_value = -1
      create = 0
      did_change = 0
      if str(param.lower_mult) == "1":
        create = 1
      else:
        create = random.randint(0, 1)
        if small_parameters_mode == 1:
          if create != 0:
            create = random.randint(0, 1)
          if create != 0:
            create = random.randint(0, 1)

      if str(create) == "0":
        continue

      x = parameter()
      x.def_name = param.def_name
      x.name = "ECUC-" + param.decl_name + "-PARAM-VALUE"
      x.param_type = param.data_type


      ###################################### IF STATEMENTS ######################################
      if str(param.data_type) == 'INTEGER':
        res_value = random.randint(int(param.start_range), int(param.end_range))
        
        if small_parameters_mode == 1:
          create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)
          if create_2 == 0:
            res_value = get_default_value(param.data_type)
            
        if res_value != get_default_value(param.data_type) or str(param.lower_mult) == "0":
          have_subedits = 1
          did_change = 1

      elif param.data_type == 'FLOAT':
        low = int((float(param.start_range) * 1000.0))
        up = 0
        if float(param.end_range) == float('inf'):
          up = 65535
        else:
          up = int((float(param.end_range) * 1000))

        res_value = random.randint(low, up)
        res_value = res_value / 1000.0

        if small_parameters_mode == 1:
          create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)
          if create_2 == 0:
            res_value = get_default_value(param.data_type)
        
        if abs(res_value - get_default_value(param.data_type)) > 0.000000001 or str(param.lower_mult) == "0":
          have_subedits = 1
          did_change = 1

      elif param.data_type == 'BOOLEAN':
        which = random.randint(0, 1)
        
        if small_parameters_mode == 1:
          create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)

          if create_2 != 0:        
            if which == 0:
              res_value = "False"
              if str(param.lower_mult) == "0":
                have_subedits = 1
                did_change = 1
            else:
              res_value = "True"
              have_subedits = 1
              did_change = 1
          else:
            res_value = "False"
            if str(param.lower_mult) == "0":
              have_subedits = 1              
              did_change = 1
        else:
          if which == 0:
            res_value = "False"
            if str(param.lower_mult) == "0":
              have_subedits = 1
              did_change = 1
          else:
            res_value = "True"
            have_subedits = 1
            did_change = 1
            

      elif str(param.data_type) == 'ENUMERATION':
        tempidx = random.randint(0, 2)
        res_value = enumer_value[tempidx]
        
        if small_parameters_mode == 1:
          create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)
          if create_2 != 0:
            create_2 = random.randint(0, 1)
          if create_2 == 0:
            res_value = get_default_value(param.data_type)
            
        if res_value != enumer_value[0] or str(param.lower_mult) == "0":
          have_subedits = 1
          did_change = 1
          
      ###########################################################################################

      x.value.set_text(str(res_value))
      sets[node].parameters.append(x)
      sets[node].param_default.append(did_change)


    if have_subedits == 0:
      lout.write("Create " + str(sets[node].num_containers) + " " + container_def[k].name + " containers;\n")
    else:
      lout.write("Create " + str(sets[node].num_containers) + " " + container_def[k].name + " containers and in each: -\n")
      idx0 = 0
      for param in sets[node].parameters:
        if sets[node].param_default[idx0] == 0:
          idx0 += 1
          continue

        d = depth + 1
        while d > 0:
          lout.write("-")
          d -= 1
        lout.write(" ")
        lout.write("Set the value of " + param.def_name + " to " + str(param.value.text) + ";\n")

        idx0 += 1

      for child in sets[node].subsets:
        if sets[child].num_containers > 0:
          dfs(child, depth + 1)

  dfs(0, 0)
  file_name = "sets" + str(i + 1) + ".xml"

  containers_bfs = [container()]
  set_index = [0]
  real_par = [-1] # the actual parent, not the parsing parent
  
  containers_bfs[0].parent_def_ref.text = "/AUTOSAR/EcucDefs/CanNm"

  def dfs2(node):
    a1 = set_index[node]
    a2 = sets[a1].parse_index
    # if str(container_def[a2].name) == "CanNmChannelConfig":
    #   print(container_def[a2].name)
    #   print(node)

    containers_bfs[node].short_name.set_text(container_def[a2].name)
    containers_bfs[node].set_in_tag_value(["UUID", container_def[a2].UUID])

    if node > 0:
      containers_bfs[node].parent_def_ref = containers_bfs[real_par[node]].def_ref
    containers_bfs[node].declare_container()
    if node > 0:
      containers_bfs[real_par[node]].add_child(containers_bfs[node])
    
    for param in sets[a1].parameters:
      param.parent_def_ref = containers_bfs[node].def_ref
      param.declare_parameter()
      containers_bfs[node].add_parameter(param)

    for child in sets[a1].subsets:
      if sets[child].num_containers > 0:
        total = sets[child].num_containers
        # if str(container_def[a2].name) == "CanNmGlobalConfig":
        #   print("In node: ", container_def[a2].name, " child: ", container_def[sets[child].parse_index].name, " total: ", total)
        while total > 0:
          containers_bfs.append(container())
          set_index.append(child)
          real_par.append(node)
          total -= 1
        # print(len(containers_bfs))
        total = sets[child].num_containers
        cur_len = len(containers_bfs)
        # print(total)
        while total > 0:
          # if str(container_def[a2].name) == "CanNmGlobalConfig":
          #   print("Going to dfs2 with: ", cur_len - total)
          dfs2(cur_len - total)
          total -= 1


  dfs2(0)
  et_elements[0].append(et_elements[containers_bfs[0].et_element_idx])
  final_out(file_name, AS)


  
###############

"""
The final idea? 
Create class called "Set" containing all the needed data for the number of classes


What are the possibilities? 
We can create a set of identical subcontainers with identical parameters (added or removed, not default, ...etc)
and each of these subcontainers can have SETS of identical subcontainers with identical parameters (added or removed, not default, ...etc)

You can treat the upper-multiplicity of 1 as a parameter, add it or remove it.

Now, what's the big idea? How to create a syntax for this?

(Synonmys 3la el model)
(Restructring of the syntax 3la el model)

Every set can have its own line, if you're going to create something for it, you can do 
"Create 20 something something, each having: 
1. *Talk about the parameters*
2. *Talk about the singular subcontainers*
3. *Talk about a set of multiple subcontainers*, each having:
3.1. *Talk about the parameters*"
4. *Talk about a set of multiple subcontainers*, each having:
4.1. *Talk about the parameters*
..."

IDEA: use context-free grammars

Another problem: the default subcontainers. How to deal with them?
If you don't say: "Create 20 something something" then there will be a default subcontainer.
The user will have the right to change the parameters and/or the children of that subcontainer.
So, we need to have files that have the default subcontainers with the user changing the parameters.

SO, we will create two types of files?

You can make the grammar able to change the parameters of a subcontainer without creating a file through the grammar.

The idea is to create the tree before parsing it into et elements.

Now, there are two problems:
1. How to structure the tree? (What objects inside the tree to make the tree possible and store the data)
2. How to randomize according to the structure of the tree
3. How to parse the tree into et elements

Now, in 1, we can try to use the given datastructures. We can think about it for a bit
"""