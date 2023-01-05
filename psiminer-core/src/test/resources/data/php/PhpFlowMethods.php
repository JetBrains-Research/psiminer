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
        $a = 0;
        $b = $a + 1;
        $c = $a + $b;
        $d = $c * $c;
    }

    public function ifMethod()
    {
        $a = 0;
        if ($a > 0) {
            $b = 1;
        } else {
            $b = 2;
        }
        $b = $b + 1;
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
}