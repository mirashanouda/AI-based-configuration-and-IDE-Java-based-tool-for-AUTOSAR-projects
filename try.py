# x = "mohamed"


# for i in range(5):
#     x += " mohamed"
# def func():
#     global x
#     x += " abdelsalam"
    
# for i in range(5):
#     func()
    
# print(x)
# print("\nModule:\n\tName: CanNm\n\tContainer:\n\t\t- Name: CanNmGlobalConfig\n\t\t- Container:\n\t\t\t- Name: CanNmChannelConfig\n\t\t\t- Multiplicity: 23\n\t\t\t- Parameter:\n\t\t\t\tName: CanNmMsgTimeoutTime\n\t\t\t\tValue: 3.728\n\t\t\t- Container:\n\t\t\t\t- Name: CanNmRxPdu\n\t\t\t\t- Multiplicity: 42\n\t\t\t\t- Parameter:\n\t\t\t\t\tName: CanNmRxPduId\n\t\t\t\t\tValue: 9719\n")
# print("\nModule:\n\tName: CanNm\n\tContainer:\n\t\t- Name: CanNmGlobalConfig\n\t\t- Container:\n\t\t\t- Name: CanNmChannelConfig\n\t\t\t- Multiplicity: 10\n\t\t\t- Parameter:\n\t\t\t\tName: CanNmNodeIdEnabled\n\t\t\t\tValue: True\n\t\t\t- Container:\n\t\t\t\t- Name: CanNmRxPdu\n\t\t\t\t- Multiplicity: 2\n\t\t\t\t- Parameter:\n\t\t\t\t\tName: CanNmRxPduId\n\t\t\t\t\tValue: 41843\n")
# import random

# n = 5
# whole = []
# for i in range(n):
#     whole.append(i)
  
# # generating a random permutation
# selected = []
# back_idx = n - 1
# for _ in range(n):
#     sel_idx = random.randint(0, back_idx)
#     selected.append(whole[sel_idx])
    
#     # swap(whole[sel_idx], whole[back_idx])
#     temp = whole[sel_idx]
#     whole[sel_idx] = whole[back_idx]
#     whole[back_idx] = temp
    
#     back_idx -= 1
    
# print(selected)
    
# import json
# f = open('trial_dataset.json',)
# data = json.load(f)

# with open('outtext.txt', 'w') as ff:
#     counter = 0
#     for i in data['dataset']:
#         counter += 1
#         if counter > 10:
#             break
#         ff.write(i['text'])
#         ff.write("\n")
#         ff.write("\n")

# def func(x):
#   x += 1


# x = 5
# func(x)
# print(x)

print("\nModule:\n\tName: CanNm\n\tContainer:\n\t\t- Name: CanNmGlobalConfig\n\t\t- Container:\n\t\t\t- Name: CanNmChannelConfig\n\t\t\t- Multiplicity: 11\n\t\t\t- Parameter:\n\t\t\t\tName: CanNmCarWakeUpBytePosition\n\t\t\t\tValue: 0\n\t\t- Container:\n\t\t\t- Name: CanNmChannelConfig\n\t\t\t- Multiplicity: 8\n\t\t\t- Parameter:\n\t\t\t\tName: CanNmImmediateNmTransmissions\n\t\t\t\tValue: 197\n\t\t- Container:\n\t\t\t- Name: CanNmChannelConfig\n\t\t\t- Multiplicity: 10\n\t\t\t- Parameter:\n\t\t\t\tName: CanNmActiveWakeupBitEnabled\n\t\t\t\tValue: True\n\t\t- Container:\n\t\t\t- Name: CanNmChannelConfig\n\t\t\t- Multiplicity: 24\n\t\t\t- Parameter:\n\t\t\t\tName: CanNmTimeoutTime\n\t\t\t\tValue: 12.38\n")