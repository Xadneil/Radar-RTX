'''
File: login_server.py
Auth: Jim Ching
'''
import random                          # Randomize authitentication id addition
import sys                             # Exiting the process
import os                              # Random authentication identifier
import signal                          # Termination Signal
import multiprocessing                 # Terminate Process
import socket                          # TCP sockets
import sqlalchemy                      # Sqlite3 DB
import sqlalchemy.orm                  # Object Relational Mapper
import sqlalchemy.ext.declarative      # Declarative System
import threading                       # Thread Client Connections
import struct                          # Packing and unpacking data
import colorama

# login server globals
debugmode = True
lserv_sock = None                      # login server socket
lserv_stdout = None                    # login server pipe
lserv_table = None                     # global login user table

# synchronization globals
lserv_auth = os.urandom(16)            # initial random authentication id
lserv_auth_lock = threading.Lock()     # global lock on modifying auth id
lserv_stdout_lock = threading.Lock()   # global lock on stdout

# initialize engine, declarative system, and session
db_engine = sqlalchemy.create_engine('sqlite:///login.db', isolation_level = 'SERIALIZABLE')
db_base = sqlalchemy.ext.declarative.declarative_base()
db_session = sqlalchemy.orm.sessionmaker()
db_session = sqlalchemy.orm.scoped_session(db_session)

# user database
class MiniMapMember(db_base):
   __tablename__ = 'minimap_member'
   email = sqlalchemy.Column(sqlalchemy.String(64), primary_key = True)
   password = sqlalchemy.Column(sqlalchemy.String(64))
   privilege = sqlalchemy.Column(sqlalchemy.Integer)
   def __str__(self):
      return 'User: %25s %25s %25s' % (self.email,self.password,self.privilege)

# termination signal
def async_shutdown(stack_name, stack_frame):
   "Core server's termination signal to login server."
   global lserv_sock, lserv_stdout
   lserv_sock.close()                                 # close network connection
   lserv_stdout.close()
   sys.exit(0)

# login status code
login_status_code = {
   'LOGIN_FAIL'      : 401,
   'LOGIN_ALREADY'   : 471,
   'LOGIN_USER'      : 200,
   'LOGIN_ADMN'      : 201,
   'REGISTER_FAIL'   : 128,
   'REGISTER_SUCCESS': 137
}

# login server packet handlers
def authent_user(packet):
   global lserv_auth, lserv_table
   # Unpack client packet
   packet_content = struct.unpack('>64s64s',packet)
   email = packet_content[0]
   email = email[:email.find(0x00)].decode('utf-8')             # truncate the \x00 bytes for email and decode
   password = packet_content[1]
   password = password[:password.find(0x00)].decode('utf-8')    # truncate the \x00 bytes for password and decode

   # Local session per client
   local_session = db_session()
   match_email = local_session.query(MiniMapMember).filter(MiniMapMember.email == email).first()
   
   # Pack server packet
   packet_header = 0xa2
   if match_email != None and not match_email.email in lserv_table:   # match passwords for LOGIN_USER or LOGIN_ADMN
      status_code = login_status_code['LOGIN_USER'] if login_status_code['LOGIN_USER'] == match_email.privilege else login_status_code['LOGIN_ADMN']
      authentication_id = lserv_auth
      event_port = 0

      # generate new authenticiation ID
      with lserv_auth_lock:
         temp_auth = bytearray(lserv_auth)
         temp_auth[random.randint(0,15)] += 1
         lserv_auth = bytes(temp_auth)

      # add user to login table
      user_entry = { email : (authentication_id, status_code) }
      lserv_table.update(user_entry)
   else:             # ummatch email for LOGIN_FAIL
      if match_email != None and match_email.email in lserv_table:
         status_code = login_status_code['LOGIN_ALREADY']
         del lserv_table[match_email.email]
      else:
         status_code = login_status_code['LOGIN_FAIL']
      authentication_id = b'0'
      event_port = 0

   # Display incoming and outgoing packet
   if debugmode:
      with lserv_stdout_lock:
         lserv_stdout.send('packet header %s' % (colorama.Fore.GREEN + str(int('0xa1',16)) + colorama.Fore.WHITE))
         lserv_stdout.send('email %s' % (colorama.Fore.GREEN + email + colorama.Fore.WHITE))
         lserv_stdout.send('password %s' % (colorama.Fore.GREEN + password + colorama.Fore.WHITE))
         lserv_stdout.send('packet header %s' % (colorama.Fore.YELLOW + str(packet_header) + colorama.Fore.WHITE))
         lserv_stdout.send('status_code %s' % (colorama.Fore.YELLOW + str(status_code) + colorama.Fore.WHITE))
         lserv_stdout.send('authentication_id %s' % (colorama.Fore.YELLOW + str(authentication_id) + colorama.Fore.WHITE))
         lserv_stdout.send('event_port %s' % (colorama.Fore.YELLOW + str(event_port) + colorama.Fore.WHITE))

   local_session.close()
   return struct.pack('>2h16sh', packet_header, status_code, authentication_id, event_port)

def register_user(packet): 
   'Register a new minimap member'
   # TODO: Validation of email and password!

   # Unpack client packet
   packet_content = struct.unpack('>64s64s',packet)
   email = packet_content[0]
   email = email[:email.find(0x00)].decode('utf-8')             # truncate the \x00 bytes for email and decode
   password = packet_content[1]
   password = password[:password.find(0x00)].decode('utf-8')    # truncate the \x00 bytes for password and decode

   # Test for existence in DB
   local_session = db_session()
   exist_email = local_session.query(MiniMapMember).filter(MiniMapMember.email == email).first()

   # Don't register on the same email twice
   if exist_email == None:
      # create registeration user
      local_session.add(MiniMapMember(email = email, password = password, privilege = '200'))
      local_session.commit()

      # return successful packet
      packet_header = 0xa4
      status_code = login_status_code['REGISTER_SUCCESS']
   else:
      # return failure packet
      packet_header = 0xa4
      status_code = login_status_code['REGISTER_FAIL']

   # Return packet
   if debugmode:
      with lserv_stdout_lock:
         lserv_stdout.send('packet header %s' % (colorama.Fore.GREEN + str(int('0xa3',16)) + colorama.Fore.WHITE))
         lserv_stdout.send('email received %s' % (colorama.Fore.GREEN + email + colorama.Fore.WHITE))
         lserv_stdout.send('password received %s' % (colorama.Fore.GREEN + password + colorama.Fore.WHITE))
         lserv_stdout.send('packet header %s' % (colorama.Fore.YELLOW + str(packet_header) + colorama.Fore.WHITE))
         lserv_stdout.send('status_code %s' % (colorama.Fore.YELLOW + str(status_code) + colorama.Fore.WHITE))
   return struct.pack('>2h', packet_header, status_code)

login_packet = {
   0xa1 : authent_user,    # serv packet handler
   0xa3 : register_user    # serv packet handler
}

# login server client thread
def login_packet_handler(user_sock, user_addr):
   'Primary login server packet handler thread by login server process.'

   # display connected user's address
   with lserv_stdout_lock: lserv_stdout.send('{} connection established.'.format(user_addr))

   # handshake with the client
   while True:
      try:     # remove user from global login table when connection disconnects
         server_packet = b'\x00'
         try:  # retrieve and identify packet header
            client_packet_header = user_sock.recv(2)                       # block for client packet header
            if len(client_packet_header) <= 0: break                       # user disconnected
            packet_header = struct.unpack('>h', client_packet_header)[0]   # retrieve packet header as short
         except struct.error:                                              # skip the rest of the packet
            with lserv_stdout_lock: lserv_stdout.send('{} is sending invalid packet headers: {}'.format(user_addr, str(client_packet_header)))
            break
         else:
            if packet_header in login_packet.keys():
               client_packet_content = user_sock.recv(1024)                         # block for client packet content
               server_packet = login_packet[packet_header](client_packet_content)   # handle client packet and return server packet
            else:
               server_packet = bytearray(client_packet_header)                      # rebuilt the invalid packet header and content
               server_packet.extend(user_sock.recv(1024))         
               with lserv_stdout_lock: lserv_stdout.send('{} is sending corrupt data packet received: {}'.format(user_addr, str(server_packet)))
         user_sock.send(server_packet)                                              # send server packet
      except ConnectionAbortedError:
         if debugmode: 
            with lserv_stdout_lock: 
               lserv_stdout.send('{}'.format(sys.exc_info()[1]))    
         break
   user_sock.close()

# login server process
def login_server(host, port, lstdout, ltable):
   'Primary login server process spawn by core server.'
   global lserv_sock, lserv_stdout, lserv_table

   # setup global variables
   lserv_stdout = lstdout[1]                       # pipe connection
   lserv_table = ltable                            # global login user table

   # register signal handlers
   # avoid signal 2, 4, 6, 8, 11, 15
   signal.signal(signal.SIGTERM, async_shutdown)
   lserv_stdout.send('login server signals has been registered.')
   
   # setup the server port and socket
   lserv_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
   lserv_sock.bind((host,port))
   lserv_sock.listen(5)
   lserv_stdout.send('login server tcp socket has been initialized.')

   # setup msqlalchemy ORM to sqlite3
   db_base.metadata.bind = db_engine
   db_base.metadata.create_all()
   lserv_stdout.send('login server database has been initialized.')

   # begin listening for clients
   while True:
      user_sock, user_addr = lserv_sock.accept()   # block for connections
      threading.Thread(                            # run a new thread per client
         target = login_packet_handler, 
         args = (user_sock, user_addr)
      ).start()

   # shutdown the login server
   lserv_stdout.send('login server exit prematurely without core server consent')
   lserv_sock.close()                                 # close network connection
   multiprocessing.current_process().terminate()      # terminate login server process


# Messing around with sqlite3
if __name__ == '__main__':
   # reset all the tables
   db_base.metadata.bind = db_engine
   db_base.metadata.drop_all()
   db_base.metadata.create_all()

   # add administrator account
   init_session = db_session()
   init_session.add(MiniMapMember(email = 'tricky.loki3@gmail.com', password = 'machineheart', privilege = '201'))
   init_session.add(MiniMapMember(email = 'deflaw54@gmail.com', password = 'machinemind', privilege = '201'))
   init_session.add(MiniMapMember(email = 'david2413@gmail.com', password = 'machinemen', privilege = '201'))
   init_session.commit()

   for member in init_session.query(MiniMapMember).all():
      print(member)