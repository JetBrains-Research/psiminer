<?php

namespace data\php;

class PhpFlowMethods
{
    public function straightWriteMethod()
    {
        $a = 0;
        $b = 1;
        $c = 2;
    }

    public function straightReadWriteMethod()
    {
        $a = 1;
        $b = $a;
        $b = 2 * $a;
        $c = $a + $b;
        $d = $c * $c;
    }

    public function ifMethod()
    {
        $a = 1;
        if ($a > 1) {
            $b = 2;
        } else if ($a < 0) {
            $c = 3;
        } else {
            $d = 4;
        }
        $e = 5;
    }

    public function multipleDeclarations()
    {
        $i = 0;
        if (false) {
            $i = 2;
        } else {
            for ($i = 1; $i <= 10; $i++) {
                echo $i;
            }
        }
    }

    public function withParameter(int $a): int
    {
        $b = $a + 1;
        $a = 3;
        return $b + $a;
    }

    public function forMethod()
    {
        for ($i = 0; $i < 2; $i++) {
            if ($i == 1) {
                break;
            }
        }
    }

    public function foreachMethod()
    {
        $arr = array(1, 2, 3, 4);
        foreach ($arr as &$value) {
            $value = $value * 2;
        }
    }

    public function multipleReturns(): int
    {
        $a = 1;
        if ($a < 1) {
            return 0;
        }
        if ($a > 1) {
            if ($a > 2) {
                return 1;
            }
        }
        return 2;
    }

    public function forWithReturn()
    {
        for ($i = 0; $i < 2; $i++) {
            if ($i == 1) {
                return;
            }
        }
        $e = 5;
    }
}