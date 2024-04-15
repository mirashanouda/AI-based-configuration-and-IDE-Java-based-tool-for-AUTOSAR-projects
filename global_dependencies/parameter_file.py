import global_vars
import tag_file

class parameter(tag_file.tag):

    def __init__(self):
        self.def_name = "" # the actual parameter name
        self.def_ref = tag_file.tag()
        self.value = tag_file.tag()
        self.param_type = ""  # float or boolean string
        self.par_cont = -1 # index of parent in the array of containers
        self.parent_def_ref = tag_file.tag()

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