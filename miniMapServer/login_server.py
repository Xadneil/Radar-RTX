'''
File: login_server.py
Auth: Jim Ching
'''
import random                          # Randomize authitentication id addition
import os                              # Random authentication identifier
import signal                          # Termination Signal
import multiprocessing                 # Terminate Process
import socket                          # TCP sockets
import sqlalchemy                      # Sqlite3 DB
import sqlalchemy.orm                  # Object Relational Mapper
import sqlalchemy.ext.declarative      # Declarative System
import threading                       # Thread Client Connections
import struct                          # Packing and unpacking data
import Crypto
import colorama

# login server globals
debugmode = True
lserv_sock = None
lserv_stdout = None
lserv_auth = os.urandom(16)
lserv_auth_lock = threading.Lock()

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
      return '%25s %25s %25s' % (self.email,self.password,self.privilege)

# termination signal
def async_shutdown(stack_name, stack_frame):
   "Core server's termination signal to login server."
   lserv_stdout.put('login server termination signals received.')
   lserv_sock.close()                                 # close network connection
   multiprocessing.current_process().terminate()      # terminate login server process

# login status code
login_status_code = {
   'LOGIN_FAIL' : 401,
   'LOGIN_USER' : 200,
   'LOGIN_ADMN' : 201
}

# login server packet handlers
def authent_user(packet):
   global lserv_auth
   # Unpack client packet
   packet_content = struct.unpack('>64s64s',packet)
   email = packet_content[0]
   email = email[:email.find(0x00)].decode('utf-8')             # truncate the \x00 bytes for email and decode
   password = packet_content[1]
   password = password[:password.find(0x00)].decode('utf-8')    # truncate the \x00 bytes for password and decode

   # Display client packet on debug mode
   if debugmode:
      lserv_stdout.put('email received %s' % (colorama.Fore.GREEN + email + colorama.Fore.WHITE))
      lserv_stdout.put('password received %s' % (colorama.Fore.GREEN + password + colorama.Fore.WHITE))

   # Local session per client
   local_session = db_session()
   match_email = local_session.query(MiniMapMember).filter(MiniMapMember.email == email).first()
   
   # Pack server packet
   packet_header = 0xa2
   if match_email:   # match passwords for LOGIN_USER or LOGIN_ADMN
      status_code = login_status_code['LOGIN_USER'] if login_status_code['LOGIN_USER'] == match_email.privilege else login_status_code['LOGIN_ADMN']
      authentication_id = lserv_auth
      event_port = 0

      # generate new authenticiation ID
      with lserv_auth_lock:
         temp_auth = bytearray(lserv_auth)
         temp_auth[random.randint(0,16)] += 1
         lserv_auth = bytes(temp_auth)

      if debugmode:
         lserv_stdout.put('matched database entry email %s' % (colorama.Fore.GREEN + match_email.email + colorama.Fore.WHITE))
         lserv_stdout.put('matched database entry password %s' % (colorama.Fore.GREEN + match_email.password + colorama.Fore.WHITE))
         lserv_stdout.put('matched database entry privilege %s' % (colorama.Fore.GREEN + str(match_email.privilege) + colorama.Fore.WHITE))
         lserv_stdout.put('pack packet status_code %s' % (colorama.Fore.GREEN + str(status_code) + colorama.Fore.WHITE))
         lserv_stdout.put('pack packet authentication_id %s' % (colorama.Fore.GREEN + str(authentication_id) + colorama.Fore.WHITE))
         lserv_stdout.put('pack packet event_port %s' % (colorama.Fore.GREEN + str(event_port) + colorama.Fore.WHITE))
   else:             # ummatch email for LOGIN_FAIL
      status_code = login_status_code['LOGIN_FAIL']
      authentication_id = 0
      event_port = 0

      if debugmode:
         lserv_stdout.put('ummatched email %s' % (colorama.Fore.RED + email + colorama.Fore.WHITE))
         lserv_stdout.put('pack packet status_code %s' % (colorama.Fore.GREEN + str(status_code) + colorama.Fore.WHITE))
         lserv_stdout.put('pack packet authentication_id %s' % (colorama.Fore.GREEN + str(authentication_id) + colorama.Fore.WHITE))
         lserv_stdout.put('pack packet event_port %s' % (colorama.Fore.GREEN + str(event_port) + colorama.Fore.WHITE))
   return struct.pack('>2h16sh', packet_header, status_code, authentication_id, event_port)

def vertify_user(packet): 
   pass

login_packet = {
   0xa1 : authent_user,    # real packet handler
   0xa2 : vertify_user     # test packet handler
}

# login server client thread
def login_packet_handler(user_sock, user_addr):
   'Primary login server packet handler thread by login server process.'
   # Retrieve the packet header
   client_packet_header = user_sock.recv(2)
   packet_header = struct.unpack('>h', client_packet_header)[0]
   
   # Retrieve the packet content and call packet handler
   client_packet_content = user_sock.recv(1024)
   server_packet = login_packet[packet_header](client_packet_content)

   # Send back a message to the client and close the socket
   user_sock.send(server_packet)
   user_sock.close()

# login server process
def login_server(host, port, lstdout):
   'Primary login server process spawn by core server.'
   global lserv_sock, lserv_stdout

   # initial admin user
   '''
   init_session = db_session()
   init_session.add(MiniMapMember(email = 'tricky.loki3@gmail.com', password = 'machinehearts', privilege = '201'))
   init_session.commit()
   '''
   # queue for messages
   lserv_stdout = lstdout

   # register signal handlers
   # avoid signal 2, 4, 6, 8, 11, 15
   signal.signal(signal.SIGTERM, async_shutdown)
   lstdout.put('login server signals has been registered.')
   
   # setup the server port and socket
   lserv_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
   lserv_sock.bind((host,port))
   lserv_sock.listen(5)
   lstdout.put('login server tcp socket has been initialized.')

   # setup msqlalchemy ORM to sqlite3
   db_base.metadata.bind = db_engine
   db_base.metadata.create_all()
   lstdout.put('login server database has been initialized.')

   # begin listening for clients
   while True:
      user_sock, user_addr = lserv_sock.accept()   # block for connections
      lstdout.put('received connection from {}'.format(user_addr))                       # track client connection messages
      threading.Thread(                            # run a new thread per client
         target = login_packet_handler, 
         args = (user_sock, user_addr)
      ).start()

   # shutdown the login server
   lserv_sock.close()                                 # close network connection
   multiprocessing.current_process().terminate()      # terminate login server process


# Messing around with sqlite3
if __name__ == '__main__':
   db_base.metadata.bind = db_engine
   db_base.metadata.drop_all()
   db_base.metadata.create_all()

   init_session = db_session()
   init_session.add(MiniMapMember(email = 'tricky.loki3@gmail.com', password = 'machinehearts', privilege = '201'))
   init_session.commit()