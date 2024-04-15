def count(data, key):
    res = 0
    for item in data:
        if str(key) in item:
            res += 1
    return res

def retrieve_single(data, key):
    for item in data:
        if str(key) in item:
            return item[str(key)]
    return None

def retrieve_list(data, key):
    res = []
    for item in data:
        if str(key) in item:
            res.append(item[str(key)])
    return res

# Convension is to return what's after the colon of the key