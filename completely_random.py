import xml.etree.ElementTree as ET
from lxml import etree
import random

def initialize(et_elements):
  AUTOSAR = ET.Element("AUTOSAR")
  AUTOSAR.set("xmlns", "http://autosar.org/schema/r4.0")
  AUTOSAR.set("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
  AUTOSAR.set("xsi:schemaLocation", "http://autosar.org/schema/r4.0 AUTOSAR_00046.xsd")

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
  def_name = ""
  value = -1
  data_type = ""
  decl_name = ""
  is_default = 1
  start_range = 1
  end_range = 1
  lower_mult = -1
  upper_mult = -1

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
  name = ""
  UUID = ""
  lower_mult = -1
  upper_mult = -1

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

def parameter_sentence(container_name, parameter_name, parameter_value, theorder):
  writing = "Set the value of parameter " + parameter_name
  writing += " inside container " + container_name
  writing += " numbered " + str(theorder)
  writing += " to be " + parameter_value
  return writing

num_files = 100
max_rand_value = 150

for i in range(num_files):
  label_name = "random" + str(i + 1) + "_label" + ".txt"
  lout = open(label_name, "a")
  file_name = "random" + str(i + 1) + ".xml"

  containers_bfs = [container()]
  bfs = [0]
  parse_index = [0]
  real_par = [-1]
  total_occ = [0] * 7
  total_occ[0] = 1
  myorder = [1]

  lout.write("Create a CanNm module with CanNmGlobalConfig" + "\n")

  containers_bfs[0].parent_def_ref.text = "/AUTOSAR/EcucDefs/CanNm"
  j = 0
  while j < len(bfs):
    node = bfs[j]
    k = parse_index[j]
    theorder = myorder[j]

    containers_bfs[node].short_name.set_text(container_def[k].name)
    containers_bfs[node].set_in_tag_value(["UUID", container_def[k].UUID])

    if j > 0:
      containers_bfs[node].parent_def_ref = containers_bfs[real_par[node]].def_ref

    containers_bfs[node].declare_container()

    if j > 0:
      containers_bfs[real_par[node]].add_child(containers_bfs[node])
    
    for v in container_parameters[node]:
      res_value = -1
      create = 0
      if v.lower_mult == 1:
        create = 1
      else:
        create = random.randint(0, 1)
      if create == 0:
        continue

      x = parameter()
      x.def_name = v.def_name
      x.name = "ECUC-" + v.decl_name + "-PARAM-VALUE"
      x.param_type = v.data_type

      if v.data_type == 'INTEGER':
        res_value = random.randint(int(v.start_range), int(v.end_range))
        if res_value != get_default_value(v.data_type) or v.lower_mult == 0:
          lout.write(parameter_sentence(container_def[k].name, v.def_name, str(res_value), theorder) + "\n")

      elif v.data_type == 'FLOAT':
        low = int((float(v.start_range) * 1000.0))
        up = 0
        if float(v.end_range) == float('inf'):
          up = 65535
        else:
          up = int((float(v.end_range) * 1000))

        res_value = random.randint(low, up)
        res_value = res_value / 1000.0
        if abs(res_value - get_default_value(v.data_type)) > 0.000000001 or v.lower_mult == 0:
          lout.write(parameter_sentence(container_def[k].name, v.def_name, str(res_value), theorder) + "\n")

      elif v.data_type == 'BOOLEAN':
        res_value = "False"
        if v.lower_mult == 0:
          lout.write(parameter_sentence(container_def[k].name, v.def_name, "False", theorder) + "\n")

      elif v.data_type == 'ENUMERATION':
        tempidx = random.randint(0, 2)
        res_value = enumer_value[tempidx]
        if res_value != enumer_value[0] or v.lower_mult == 0:
          lout.write(parameter_sentence(container_def[k].name, v.def_name, str(res_value), theorder) + "\n")

      # if res_value == -1:
      #   print(v.data_type)
      x.value.set_text(str(res_value))
      x.parent_def_ref = containers_bfs[node].def_ref
      x.declare_parameter()
      containers_bfs[node].add_parameter(x)

    # we create children as we go, but parents are assumed
    for u in children[k]:
      low = float(container_def[u].lower_mult)
      up = float(container_def[u].upper_mult)
      up = min(up, float(max_rand_value))
      occ = random.randint(int(low), int(up))

      writing = "Create " + str(occ) + " subcontainers " + container_def[u].name
      writing += " for container " + container_def[k].name + " numbered " + str(theorder)

      if occ > 0:
        lout.write(writing + "\n")

      for i in range(occ):
        temp = len(bfs)
        bfs.append(temp)
        parse_index.append(u)
        containers_bfs.append(container())
        real_par.append(node)
        total_occ[u] += 1
        myorder.append(total_occ[u])

    j += 1

  et_elements[0].append(et_elements[containers_bfs[0].et_element_idx])
  final_out(file_name, AS)


# containers_bfs = [container() for _ in range(max_nodes)] 
# bfs = [0]

# containers_bfs[0].parent_def_ref.text = "/AUTOSAR/EcucDefs/CanNm"
# j = 0
# while j < len(bfs):
#     node = bfs[j]

#     containers_bfs[node].short_name.set_text(container_def[node].name)
#     containers_bfs[node].set_in_tag_value(["UUID", container_def[node].UUID])
#     if j > 0:
#       containers_bfs[node].parent_def_ref = containers_bfs[par[node]].def_ref

#     containers_bfs[node].declare_container()

#     if j > 0:
#       containers_bfs[par[node]].add_child(containers_bfs[node])

#     for v in container_parameters[node]:
#         x = parameter()
#         x.def_name = v.def_name
#         x.name = "ECUC-" + v.decl_name + "-PARAM-VALUE"
#         x.param_type = v.data_type
#         x.value.set_text(str(v.value))
#         x.parent_def_ref = containers_bfs[node].def_ref
#         x.declare_parameter()
#         containers_bfs[node].add_parameter(x)

#     for u in children[node]:
#         bfs.append(u)
#     j += 1

# et_elements[0].append(et_elements[containers_bfs[0].et_element_idx])
# final_out("containers_and_parameters_testing.xml", AS)
