if redis.call('EXISTS', KEYS[2]) == 0 then
    return 0
end
redis.call('INCR', KEYS[1])
redis.call('DEL', KEYS[2])
return 1
