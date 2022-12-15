<?php

class Base
{
    public function foo(int $a) {
        $b = 0;
        $b = $a + 1;
        $c = $b;
        return $c + 1;
    }
}