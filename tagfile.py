import xml.etree.ElementTree as ET
from lxml import etree
import random
import et_init

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
            et_init.et_init.et_elements.append(ET.Element(self.name))
            self.et_element_idx = len(et_init.et_elements) - 1

            if self.text != "":
                et_init.et_elements[self.et_element_idx].text = self.text

            if self.in_tag_value[0] != "":
                et_init.et_elements[self.et_element_idx].set(self.in_tag_value[0], self.in_tag_value[1])

        else:
            et_init.et_subelements.append(ET.SubElement(et_init.et_elements[self.par], self.name))
            self.et_element_idx = len(et_init.et_subelements) - 1

            if self.text != "":
                et_init.et_subelements[self.et_element_idx].text = self.text

            if self.in_tag_value[0] != "":
                et_init.et_subelements[self.et_element_idx].set(self.in_tag_value[0], self.in_tag_value[1])

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