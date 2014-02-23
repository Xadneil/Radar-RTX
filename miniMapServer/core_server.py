'''
File: core_server.py
Auth: Jim Ching

Documentation:
   packet_specification.txt         :  describe packet design
   core_server_specification.txt    :  describe core server design
   login_server_specification.txt   :  describe login server design

Changelog:
   [1000] - Initial core server backbone.
   [1001] - Clean the code and added colors.
   [1002] - Initial login server.
'''

import os                     # OS Servicess
import sys                    # Environment
import queue                  # Queue empty exception
import subprocess             # Execute shell commands
import signal                 # Asynchronous signals
import multiprocessing        # Spawn login, event, and map server
import threading              # Track server queues
import colorama               # Color shell text

# Login, Event, Map Server
import login_server

# server version encoding
ver_app   = 'miniMap'         # Application Name
ver_proj  = 'Yggdrasil'       # Project Name
ver_pver  = '1.0'             # Project Version
ver_encd  = 'utf-8'           # Default Encoding

# server configuration
serv_port = 33600             # Server Port [33600 - 33700]
serv_host = ''                # Server Host
lserv_name = 'mini-login'     # Login server process name
eserv_name = 'mini-event'     # Event server process name
mserv_name = 'mini-map'       # Map server process name

# core server global
serv_login = None
serv_login_queue = None
serv_login_track = None
thread_global_lock = threading.Lock()

# console text formatting functions
def printfm(pref, cont):
   'Console print formatting for two strings.'
   with thread_global_lock:
    print('{:.<30s}{:.>20s}'.format(pref, cont))   

def printfmv(pref, pref_s, cont, cont_s):
   'Console print formatting for two strings with variable width.'
   with thread_global_lock:
    print(('{:.<%ds}{:.>%ds}' % (pref_s,cont_s)).format(pref, cont))  

def printsep(len):
   'Print server line separators.'
   with thread_global_lock:
    print('%s' % ('=' * len))

def printerr(err):
  'Print exception messages with color'
  with thread_global_lock:
    print(colorama.Fore.RED + colorama.Style.BRIGHT + 'errr:' + colorama.Fore.WHITE + colorama.Style.NORMAL, err)

def printlserv(mes):
  'Print login server messages with color'
  with thread_global_lock:
    print('\r' + colorama.Fore.YELLOW + colorama.Style.BRIGHT + 'lsev:' + colorama.Fore.WHITE + colorama.Style.NORMAL, mes)

# asynchronous signal handlers
def ctrlc_exit(signal_name, signal_frame): 
   'Exit core server on ctrl + c.'
   cmd_shutdown()
   sys.exit()

# core server command handlers
def server_status(serv_process):
  'Display server process status attributes.'
  printfm('name', str(serv_process.name))
  printfm('pid', str(serv_process.pid))
  printfm('daemon', str(serv_process.daemon))
  printfm('exitcode', str(serv_process.exitcode))
  printfm('alive', str(serv_process.is_alive()))

def cmd_showmenu():
  'Display all available commands registered in core server menu.'
  for menu_item in sorted(cmd_menu_help, key = lambda contain: contain[0]):
    printfmv(menu_item,15,cmd_menu_help[menu_item],35)

def cmd_showstat():
  'Display all server processes and status'
  for server_process in multiprocessing.active_children():
    server_status(server_process)

def cmd_shutdown():
  'Signal all active servers to shutdown, release resources, and close core server.'
  cmd_loginclose()
  sys.exit()

def cmd_loginstart():
  'Start the login server with standard configuration.'
  global serv_login, serv_login_queue, serv_port
  # Support only one login server
  if serv_login != None:
    if serv_login.is_alive():
      printerr('login server has already started.')
      return
  # Generate new login server
  serv_login_queue = multiprocessing.Queue()
  serv_login = multiprocessing.Process(target = login_server.login_server, args = (serv_host, serv_port, serv_login_queue), name = lserv_name + str(serv_port))
  serv_login.daemon = True
  serv_login.start()
  server_status(serv_login)
  serv_port += 1

def cmd_logintrack():
  'Read login server messages from queue.'
  global serv_login_track
  if serv_login == None or serv_login_queue == None: return
  if serv_login_track == None or not serv_login_track.is_alive():
    serv_login_track = threading.Thread(target = logintrack)
    serv_login_track.daemon = True
    serv_login_track.start()
  else:
    printerr('login server tracking has already started.')
  
def logintrack():
  while True:
      try: printlserv(serv_login_queue.get(block = True)) # retrieve output messages
      except queue.Empty: pass                            # block for more messages
      except EOFError: break                              # login server closed
      except IOError: break                               # login server queue closed

def cmd_loginclose():
  'Terminate login server'
  if serv_login != None:
    if serv_login.is_alive():
      printfmv('info',5,'killing %s (%d)' % (serv_login.name, serv_login.pid),45)
      cmd_logintrack()
      os.kill(serv_login.pid,signal.SIGTERM)                    # send login server SIGTERM
      serv_login_queue.close()                                  # close the queue

cmd_menu_help = {
   '1.showmenu'     : 'list available commands',
   '2.showstatus'   : 'list server process status',
   '3.loginstart'   : 'start login server process',
   '4.logintrack'   : 'track login server messages',
   '5.loginclose'   : 'close login server process',
   '6.shutdown'     : 'shutdown all servers'
}

cmd_menu =  {
   'showmenu'       : cmd_showmenu,
   'showstatus'     : cmd_showstat,
   'loginstart'     : cmd_loginstart,
   'logintrack'     : cmd_logintrack,
   'loginclose'     : cmd_loginclose,
   'shutdown'       : cmd_shutdown
}   

# Core server must be execute as top-level script
if __name__ == '__main__':
  # set color settings
  colorama.init()             

  # system and version information
  printsep(50)
  printfm('app', ver_app)
  printfm('proj', ver_proj)
  printfm('pver', ver_pver)
  printfm('encd', ver_encd)
  printfm('defe', sys.getdefaultencoding())
  printfm('plat', sys.platform)
  printfm('core', str(multiprocessing.cpu_count()))
  printsep(50)
  
  # set utf-8 encoding for win32
  if sys.platform == 'win32':
     subprocess.call('chcp 65001', shell = True)                             # set console encoding to utf-8
     subprocess.call('set PYTHONIOENCODING = %s' % ver_encd, shell = True)   # set python encoding to utf-8
     printfm('info: setting console to utf-8', colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)
     printfm('info: setting python to utf-8', colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)

  # set asynchronous signals
  signal.signal(signal.SIGINT, ctrlc_exit)
  signal.signal(signal.SIGTERM, ctrlc_exit)
  #signal.signal(signal.SIGALRM, track_serv)
  #signal.setitimer(signal.ITIMER_REAL, 5, 5)
  printfm('info: registering SIGINT', colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)
  printfm('info: registering SIGTERM', colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)
  #printfm('info: registering SIGALRM', str(signal.SIGALRM) + '/' + colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)
  printsep(50)

  # display initial command list
  cmd_menu['showmenu']()
  printsep(50)

  # core server commands
  while True:
    print(colorama.Fore.WHITE + colorama.Style.BRIGHT + 'comm: ' + colorama.Style.NORMAL, end = '')
    cmd = input()                             # retrieve user command and arguments
    cmd = cmd.split(' ')                      # parse user command
    if cmd[0] in cmd_menu: cmd_menu[cmd[0]]() # execute user command