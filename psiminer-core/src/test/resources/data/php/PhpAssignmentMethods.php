<?php

namespace data\php;

class PhpAssignmentMethods
{
    public function straightAssignments()
    {
        $a = 0;
        $b = $c = array();
    }

    public function multiAssignment()
    {
        $a = $b = "ab";
        [$c, $d] = array($a, $b);
    }

    public function selfAssignment()
    {
        $a = 0;
        $a += 4;
    }
}