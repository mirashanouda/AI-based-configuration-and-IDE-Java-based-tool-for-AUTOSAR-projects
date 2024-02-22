import xml.etree.ElementTree as ET
from lxml import etree
import random
import et_init
import tagfile

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