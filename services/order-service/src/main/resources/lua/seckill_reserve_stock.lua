local stock = redis.call('GET', KEYS[1])
if not stock then
    return 3
end
if redis.call('EXISTS', KEYS[2]) == 1 then
    return 2
end
if tonumber(stock) <= 0 then
    return 1
end
redis.call('DECR', KEYS[1])
redis.call('SET', KEYS[2], ARGV[1], 'PX', ARGV[2])
return 0
