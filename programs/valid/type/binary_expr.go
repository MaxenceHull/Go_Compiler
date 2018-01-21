package main

var _bool bool
var max = "max"
var a bool = true || _bool
var b bool = _bool && _bool
var c bool = 3 < 6
var d bool = 3.0 < 6.0
var e bool = "lala" < "bb"
var f bool = 'l' < 'm'
var g bool = 3 <= 6
var h bool = 3 > 6
var i bool = 3 >= 6
var j string = "lala" + max
var k int = 3 + 5
var l float64 = 3.0 + 5.0
var m rune = 'l' + 'm'
var n int = 3 - 5
var o float64 = 3.0 * 5.0
var p rune = 'l' / 'm'
var q int = 3 % 5
var r int = 3 | 5
var s int = 3 & 5
var t int = 3 << 4
var u int = 3 >> 5
var v int = 3 &^ 5
var w int = 3 ^ 5
var x bool = max != max
var y bool = max == max
var z bool = 6 == 6
var aa bool = 6.0 == 6.0
var ab bool = "yolo" == max
var ac bool = 6 != 6
var ad bool = 6.0 != 6.0
var ae bool = "yolo" != max

var af [6]int;
var ag [6]int;
var ah bool = af == ag;
type array [5]float64
var ai array
var al array
var am bool = ai != al
