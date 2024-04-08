import xml.etree.ElementTree as ET

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