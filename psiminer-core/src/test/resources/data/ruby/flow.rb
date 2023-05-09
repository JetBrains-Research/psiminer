def simple
  a = 0
  x = y = a
  a + x + y
end

def if_method(x)
  if x > 2
    puts "x is greater than 2"
  elsif x <= 2 and x!=0
    puts "x is 1"
  else
    puts "I can't guess the number"
  end
  x
end

def for_method
  for a in 1..5 do
    puts a
  end
end

def for_break_method
  for i in 0..5
    if i > 2
      break
    end
    puts "Value of local variable is #{i}"
  end
end