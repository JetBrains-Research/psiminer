def simple
  a = 0
  b = a if false
  x = y = a
  b + x + y
end

def abbreviated
  a = 1
  a += a
  a ||= a
end

def multiple(e)
  a = 1, 2, 3
  a, b = e, e
  (c, d) = [a, b]
  a, (b, *c), *d = e, [e, 3, 4], 5, 6
end

def with_func()
  a = 1
  b, c = a, multiple(a)
end

$global_variable = 10

def to_global
  local = $global_variable
  local2 = local
end

def to_param(a)
  b = a
  return a + b
end

class Customer
  @@no_of_customers
  def initialize(id, name, addr)
    @cust_id = id
    @cust_name = name
    @cust_addr = addr
    @@no_of_customers = addr
  end
  def display_details()
    puts "Customer id #@cust_id"
    puts "Customer name #@cust_name"
    puts "Customer address #@cust_addr"
    @cust_id = @@no_of_customers
  end
end