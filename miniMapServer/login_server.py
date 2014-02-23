'''
File: login_server.py
Auth: Jim Ching
'''
import signal                          # Termination Signal
import multiprocessing                 # Terminate Process
import socket                          # TCP sockets
import sqlalchemy                      # Sqlite3 DB
import sqlalchemy.orm                  # Object Relational Mapper
import sqlalchemy.ext.declarative      # Declarative System
import threading                       # Thread Client Connections
import struct

# login server globals
lserv_sock = None

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

def async_shutdown(stack_name, stack_frame):
   "Core server's termination signal to login server."
   lstdout.put('login server termination signals received.')
   lserv_sock.close()                                 # close network connection
   multiprocessing.current_process().terminate()      # terminate login server process


# login server packet handlers
def authent_user(packet): pass
def vertify_user(packet): pass

login_status_code = {
   'LOGIN_FAIL' : 401,
   'LOGIN_USER' : 200,
   'LOGIN_ADMN' : 201
}

login_packet = {
   0xa1 : authent_user,
   0xa2 : vertify_user
}

# login server client thread
def login_packet_handler(user_sock, user_addr):
   'Primary login server packet handler thread by login server process.'
   # IMPORTANT NOTE :
   # Current echo's back the packet
   packet = user_sock.recv()
   user_sock.send(packet)
   user_sock.close()

# login server process
def login_server(host, port, lstdout):
   'Primary login server process spawn by core server.'
   global lserv_sock

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
      lstdout.put('%s' % user_add)                 # track client connection messages
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

