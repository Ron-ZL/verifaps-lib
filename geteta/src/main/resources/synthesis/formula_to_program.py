#!/usr/bin/env python3

"""
Passes the input formula to the BDD library "omega" and returns ITE cascades for the result variables.
"""

from typing import Dict, Iterable, List, Sequence, Tuple


def _fix_remove_extra_signed_int_bits(functions: Dict[str, str], automaton_vars: List[Dict]):
    # For signed ints with range [INT_MIN, INT_MAX], omega exposes an unnecessary extra bit. This bit seems to be
    # necessary for correct calculations with INT_MIN (patching omega.logic.bitvector.dom_to_width leads to incorrect
    # BDDs), but shouldn't be exposed to the caller. Therefore, if we detect that one of the result variables is a
    # signed integer with range [INT_MIN, INT_MAX], we remove the extra generated function. Since the extra bit of the
    # input variables seems to be a sign extension, we replace its usage in other functions with the actual sign bit.
    from math import log2

    def replace_variable(target_formula: str, name: str, replacement: str) -> str:
        import re
        return re.sub(rf"([(,\s]|^){name}([),\s]|$)", rf"\1{replacement}\2", target_formula)

    for automaton_var in automaton_vars:
        if automaton_var['type'] == 'int' and automaton_var['signed']:
            assert len(automaton_var['bitnames']) >= 2
            (dom_min, dom_max) = automaton_var['dom']
            min_bits_fraction = log2(-dom_min)
            if min_bits_fraction - int(min_bits_fraction) == 0 and -dom_min == dom_max + 1:
                extra_bit = automaton_var['bitnames'][-1]
                sign_bit = automaton_var['bitnames'][-2]
                if extra_bit in functions:  # result variable?
                    del functions[extra_bit]
                else:  # input variable or result without an output function - in any case, replace usages of sign bit
                    for key, formula in functions.copy().items():
                        functions[key] = replace_variable(formula, extra_bit, sign_bit)


def formula_to_ite_cascades(formula: str, result_vars: Iterable[str], var_defs: Iterable[str]) -> Dict[str, str]:
    """
    Builds ITE cascades for calculating the given result variables from the other variables using the given formula.

    :param formula: Formula in the format expected by omega
    :param result_vars: Names of the variables to calculate
    :param var_defs: Definitions of all variables in the formula in the format var:bool or int_var[min,max]
    :return: (output_bit, ite_formula) pairs for all bits of the given result variables
    """
    from omega.symbolic.functions import make_functions
    from omega.symbolic.temporal import Automaton
    import omega.symbolic.codegen as cg

    automaton = Automaton()
    automaton_vars = {}
    for var_def in var_defs:
        if ':' in var_def:
            var_name, var_type = var_def.split(':', 1)
            automaton_vars[var_name] = var_type
        else:
            var_name, domain = var_def[:-1].split('[', 1)
            var_min, var_max = domain.split(',', 1)
            automaton_vars[var_name] = int(var_min), int(var_max)
    automaton.declare_variables(**automaton_vars)

    relation = automaton.to_bdd(formula)

    extract_vars = []
    for result_variable in result_vars:
        automaton_result_var = automaton.vars[result_variable]
        if automaton_result_var['type'] == "bool":
            extract_vars.append(result_variable)
        else:
            extract_vars += automaton_result_var['bitnames']

    #functions = make_functions(relation, extract_vars, automaton.bdd)
    #result = {value: result['function'].bdd.to_expr(result['function']) for (value, result) in functions.items()}
    #_fix_remove_extra_signed_int_bits(result, automaton.vars.values())

    code = cg.dumps_bdds_as_code(relation, extract_vars, automaton, lang='c')
    print(code)

    return None


def parse_arguments(arguments: Sequence[str]) -> Tuple[str, List[str], List[str]]:
    from argparse import ArgumentParser, ArgumentTypeError
    import re

    def var(value, pattern=re.compile(r"^[a-zA-Z_][a-zA-Z_0-9]*'?$")):
        if not pattern.match(value):
            raise ArgumentTypeError('variable names can only contain letters, underscores and numbers')
        return value

    def var_def(value, pattern=re.compile(r'^[a-zA-Z_][a-zA-Z_0-9]*(\[-?\d+,-?\d+\]|:bool)$')):
        if not pattern.match(value):
            raise ArgumentTypeError('variable definitions must be in format integer_var[min,max] or var:bool')
        return value

    parser = ArgumentParser(description=__doc__)
    parser.add_argument('formula', metavar='FORMULA', type=str, help='formula for the result variable(s)')
    parser.add_argument('--result', metavar='RESULT_VAR', type=var, required=True, action='append',
                        help='result variable (can be specified multiple times)')
    parser.add_argument('variables', metavar='VAR', type=var_def, nargs='+',
                        help='variable definitions, including result variable(s)')
    arguments = parser.parse_args(arguments)

    # very basic validation (we'd need to re-implement omega's lexer and parser to validate more)
    defined_variables = set(re.split(r'[:\[]', variable_definition)[0] for variable_definition in arguments.variables)
    result_variables = set(result.rstrip("'") for result in arguments.result)
    if result_variables - defined_variables:
        parser.error('each RESULT_VAR must be listed in the VAR definitions')

    return arguments.formula, arguments.result, arguments.variables

TEST_ARGS =[ "--result", "x" , "x + 2 * y != 0", 'x[-127,128]',  'y[-127,128]']

def main() -> None:
    import sys
    arguments = parse_arguments( TEST_ARGS if len(sys.argv)==1 else sys.argv[1:])
    ite_cascades = formula_to_ite_cascades(arguments[0], arguments[1], arguments[2])
    for bit in sorted(ite_cascades):
        print(f"{bit} = {ite_cascades[bit]}")


if __name__ == '__main__':
    main()


# Unit tests
def test_argument_parsing(capsys):
    from pytest import raises
    good_args = ['--result', '__a1', '--result', "b'", '__a1 <=> b_0', 'b[0,1]', '__a1:bool']
    assert parse_arguments(good_args) == ('__a1 <=> b_0', ['__a1', "b'"], ['b[0,1]', '__a1:bool'])

    missing_args = ['__a1 <=> b_0', 'b[0,1]', '__a1:bool']
    with raises(SystemExit):
        parse_arguments(missing_args)
    assert capsys.readouterr().err.startswith('usage:')

    bad_vars = ['--result', '__a1', '--result', "ö'", '__a1 <=> ö_0', 'ö[0,1]', '__a1:bool']
    with raises(SystemExit):
        parse_arguments(bad_vars)
    assert capsys.readouterr().err.startswith('usage:')

    missing_result_defs = ['--result', '__a1', '--result', "b'", '__a1 <=> b_0', 'b[0,1]']
    with raises(SystemExit):
        parse_arguments(missing_result_defs)
    stderr = capsys.readouterr().err
    assert stderr.startswith('usage:') and 'RESULT_VAR must be listed' in stderr


def test_ite_calculation():
    bool_test = formula_to_ite_cascades('~o <=> __history_o_1', ['o'],  ['__history_o_1:bool', 'o:bool'])
    assert len(bool_test) == 1 and bool_test.keys() == {'o'}

    int_test = formula_to_ite_cascades("y' = 2 * x + (y - 1) & y' > 0 & x' = y'", ["x'", "y'"],  ['x[1,6]', 'y[1,6]'])
    assert len(int_test) == 6 and int_test.keys() == {"x_0'", "x_1'", "x_2'", "y_0'", "y_1'", "y_2'"}

    signed_int_test = formula_to_ite_cascades("y' = (y * y)", ["y'"], ['y[-128,127]'])
    assert (len(signed_int_test) == 8 and signed_int_test.keys() == {f"y_{i}'" for i in range(0, 8)}
            and "y_8" not in " ".join(signed_int_test.values()))

    dont_care_test = formula_to_ite_cascades('TRUE', ['x'],  ['x[0,7]'])
    assert len(dont_care_test) == 0
