import xml.etree.ElementTree as ET
import et_init
import tag_file

class container(tag_file.tag):
  def __init__(self):
      self.short_name = tag_file.tag()
      self.def_ref = tag_file.tag()
      self.container_idx = -1
      self.par_cont = -1 # parent as a container
      self.sub_cont_idx = -1
      self.param_val_idx = -1
      self.has_param_val = 0
      self.has_sub_cont = 0
      self.parent_def_ref = tag_file.tag()


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
      self.param_val_idx = len(et_init.et_elements)
      et_init.et_elements.append(ET.Element("PARAMETER-VALUES"))
      et_init.et_elements[self.et_element_idx].append(et_init.et_elements[self.param_val_idx])
    et_init.et_elements[self.param_val_idx].append(et_init.et_elements[param.et_element_idx])

  def add_child(self, child):
    if self.has_sub_cont == 0:
      self.has_sub_cont = 1
      self.sub_cont_idx = len(et_init.et_elements)
      et_init.et_elements.append(ET.Element("SUB-CONTAINERS"))
      et_init.et_elements[self.et_element_idx].append(et_init.et_elements[self.sub_cont_idx])
    et_init.et_elements[self.sub_cont_idx].append(et_init.et_elements[child.et_element_idx])

  def get_par_cont(self):
    return self.par_cont

  def set_par_cont(self, value):
    self.par_cont = value

  def get_container_idx(self):
    return self.container_idx

  def set_container_idx(self, value):
    self.container_idx = value