'Test login server by sending it packets'
import os
import random
import sys
import socket
import struct
import threading
import multiprocessing

serv_host = '50.62.212.171'
#serv_host = 'localhost'
serv_port = 33600


def DDOS_BRUTE():
   header = 0xa1
   email = (''.join([chr(random.randint(0,25) + 65) for i in range(5)])).encode('utf-8')
   password = (''.join([chr(random.randint(0,25) + 65) for i in range(5)])).encode('utf-8')
   client_packet = struct.pack('>h64s64s', header, email, password)
   client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
   client_sock.connect((serv_host,serv_port))
   client_sock.send(client_packet)
   client_packet = client_sock.recv(1024)
   print(client_packet)
   client_sock.close()

if __name__ == '__main__':
   if sys.argv[1] == '0xa1':
      header = 0xa1
      email = 'tricky.loki3@gmail.com'.encode('utf-8')
      password = 'machinehearts'.encode('utf-8')
      client_packet = struct.pack('>h64s64s', header, email, password)
   elif sys.argv[1] == '0xa1l':
      header = 0xa1
      email = sys.argv[2].encode('utf-8')
      password = sys.argv[3].encode('utf-8')
      client_packet = struct.pack('>h64s64s', header, email, password)
   elif sys.argv[1] == '0xa3':
      header = 0xa3
      email = ('{}@gmail.com'.format(''.join([chr(random.randint(0,25) + 65) for i in range(5)]))).encode('utf-8')
      password = ('{}'.format(''.join([chr(random.randint(0,25) + 65) for i in range(5)]))).encode('utf-8')
      client_packet = struct.pack('>h64s64s', header, email, password)
   elif sys.argv[1] == '0xa3l':
      header = 0xa3
      email = sys.argv[2].encode('utf-8')
      password = sys.argv[3].encode('utf-8')
      client_packet = struct.pack('>h64s64s', header, email, password)
   elif sys.argv[1] == 'fakepacket':
      header = int(sys.argv[2],16)
      client_packet = struct.pack('>h', header)
   elif sys.argv[1] == 'DDOS':
      processList = [multiprocessing.Process(target = DDOS_BRUTE) for count in range(int(sys.argv[2]))]
      for process in processList: process.start()
      for process in processList: process.join()
      os._exit(1)
   else:
      print('Please enter a packet header such as 0xa1 as argument.')
      print('Usage: python login_client 0xa1')
      print('\t0xa1')
      print('\t0xa1l <email> <password>')
      print('\t0xa3')
      print('\tDDOS <packet count>')

   client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
   client_sock.connect((serv_host,serv_port))
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