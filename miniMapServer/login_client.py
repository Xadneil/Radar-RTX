'Test login server by sending it packets'
import sys
import socket
import struct

serv_host = 'localhost'
serv_port = 33600

# connect to server
client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_sock.connect((serv_host,serv_port))

header = 0xa1
email = 'tricky.loki3@gmail.com'.encode('utf-8')
password = 'machinehearts'.encode('utf-8')
client_packet = struct.pack('>h64s64s', header, email, password)

client_sock.send(client_packet)
client_packet = client_sock.recv(1024)
print(client_packet)
client_sock.close()

'''
objpack = struct.unpack('>h64s64s', client_packet)
total_bytes = 0
for ele in objpack:
   print(sys.getsizeof(ele))
   total_bytes += sys.getsizeof(ele)
print('Packet Length: ', total_bytes)

print(type(client_packet), client_packet)
print(type(objpack), objpack)
print(objpack[0], type(objpack[0]))
print(objpack[1], len(objpack[1]), type(objpack[1]))
print(objpack[2], len(objpack[2]), type(objpack[2]))
'''