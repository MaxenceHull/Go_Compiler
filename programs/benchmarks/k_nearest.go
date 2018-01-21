package main

type point struct {
  //Could easily be a 2d point if sqrt is available
  //in order to easily compute euclidian distance
  x int
  class int
}

func replace(points [10]point, p point, size int){
  for i:=0; i<size; i++ {
    if p.x == points[i].x {
      points[i].x = p.x
      break
    }
  }
}

func euclidian_distance(p1 point, p2 point) int{
  distance := p1.x - p2.x
  if distance < 0 {
    distance = -distance
  }

  return distance
}

func find_max(to_predict point, points [1000000]point, size int) point {
  var max point = points[0]
  for i:= 1; i<size; i++{
    if euclidian_distance(max, to_predict) < euclidian_distance(points[i], to_predict) {
        max = points[i]
      }
  }
  return max
}

func k_nearest(k int, to_predict point, points [1000000]point, size int) int{
  var k_near [10]point
  for i:=0; i<k; i++{
    k_near[i] = points[i]
  }
  for i:=k; i<size; i++ {
    var max point = find_max(to_predict, points, k)
    if euclidian_distance(to_predict, points[i]) < euclidian_distance(to_predict, max){
      replace(k_near, points[i], k)
    }
  }
  total_0, total_1 := 0, 0
  for i :=0; i<k; i++ {
    if k_near[i].class == 0 {
      total_0 += 1
    } else {
      total_1 += 1
    }
  }

  if total_1 > total_0 {
    return 1
  } else {
    return 0
  }
}

func main() {
  //generate database
  var db [1000000]point
  for i:=0; i < 1000000; i+=2 {
    var p point
    p.x = i
    p.class = 0
    db[i] = p
  }
  for i:=1; i < 1000000; i+=2 {
    var p point
    p.x = i
    p.class = 1
    db[i] = p
  }
  var test point
  test.x = 12
  result := k_nearest(10, test, db, 100000)
}
