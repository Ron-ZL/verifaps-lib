TYPE
  _INSTANCE_ID_TYPE : INT(-1..20) := -1;
END_TYPE

INTERFACE ITF
END_INTERFACE

CLASS A IMPLEMENTS ITF
  VAR
    b1 : B;
    b2 : B;
    c1 : C;
    c2 : C;
    _INSTANCE_ID : _INSTANCE_ID_TYPE := -1;
  END_VAR
END_CLASS

CLASS A1 EXTENDS A
  VAR
    _INSTANCE_ID : _INSTANCE_ID_TYPE := -1;
  END_VAR
END_CLASS

CLASS A2 EXTENDS A
  VAR
    _INSTANCE_ID : _INSTANCE_ID_TYPE := -1;
  END_VAR
END_CLASS

CLASS B IMPLEMENTS ITF
  VAR
    c1 : C;
    c2 : C;
    _INSTANCE_ID : _INSTANCE_ID_TYPE := -1;
  END_VAR
END_CLASS

CLASS C
  VAR
    _INSTANCE_ID : _INSTANCE_ID_TYPE := -1;
  END_VAR
END_CLASS

VAR_GLOBAL
  _INSTANCES_A : ARRAY [0..0] OF A := [
    (_INSTANCE_ID := 0, b1 := 2, b2 := 3, c1 := 11, c2 := 12)
  ];
  _INSTANCES_A1 : ARRAY [1..1] OF A1 := [
    (_INSTANCE_ID := 1, b1 := 4, b2 := 5, c1 := 17, c2 := 18)
  ];
  _INSTANCES_B : ARRAY [2..6] OF B := [
    (_INSTANCE_ID := 2, c1 := 7, c2 := 8),
    (_INSTANCE_ID := 3, c1 := 9, c2 := 10),
    (_INSTANCE_ID := 4, c1 := 13, c2 := 14),
    (_INSTANCE_ID := 5, c1 := 15, c2 := 16),
    (_INSTANCE_ID := 6, c1 := 19, c2 := 20)
  ];
  _INSTANCES_C : ARRAY [7..20] OF C := [
    (_INSTANCE_ID := 7),
    (_INSTANCE_ID := 8),
    (_INSTANCE_ID := 9),
    (_INSTANCE_ID := 10),
    (_INSTANCE_ID := 11),
    (_INSTANCE_ID := 12),
    (_INSTANCE_ID := 13),
    (_INSTANCE_ID := 14),
    (_INSTANCE_ID := 15),
    (_INSTANCE_ID := 16),
    (_INSTANCE_ID := 17),
    (_INSTANCE_ID := 18),
    (_INSTANCE_ID := 19),
    (_INSTANCE_ID := 20)
  ];
END_VAR

PROGRAM MY_PROGRAM
  VAR_INPUT
    switch : BOOL;
  END_VAR
  VAR
    i : ITF;
    r : REF_TO A := REF(a);
    a : A;
    a1 : A1;
    b : B;
  END_VAR

  IF (switch) THEN
    i := a;
    r := REF(a);
  ELSE
    i := b;
    r := REF(a1);
  END_IF

  r^.c1 := a.b1.c1;
  r^.c2 := a.b2.c2;

  r^.b1.c2 := r^.c2;
END_PROGRAM
