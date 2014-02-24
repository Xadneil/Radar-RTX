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
   [1003] - Stable core and login release.
'''
import time
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
serv_login = None                     # login server process
serv_login_comlink = None             # login server pipe
serv_login_comlink_listener = None    # login server pipe process
serv_login_user_table = None          # authenticated user table (shared) authentication id -> [list object]

# console text formatting functions
def printfm(pref, cont):
  'Console print formatting for two strings.'
  print('{:.<30s}{:.>20s}'.format(pref, cont))   

def printfmv(pref, pref_s, cont, cont_s):
  'Console print formatting for two strings with variable width.'
  print(('{:.<%ds}{:.>%ds}' % (pref_s,cont_s)).format(pref, cont))  

def printsep(len):
  'Print server line separators.'
  print('%s' % ('=' * len))

def printerr(err):
  'Print exception messages with color'
  print(colorama.Fore.RED + colorama.Style.BRIGHT + 'errr:' + colorama.Fore.WHITE + colorama.Style.NORMAL, err)

def printlserv(mes):
  'Print login server messages with color'
  print('\r' + colorama.Fore.YELLOW + colorama.Style.BRIGHT + 'lsev:' + colorama.Fore.WHITE + colorama.Style.NORMAL, mes)

# asynchronous signal handlers
def ctrlc_exit(signal_name, signal_frame): 
   'Exit core server on ctrl + c.'
   cmd_shutdown()
   os._exit(0)

# core server command handlers
def server_status(serv_process):
  'Display server process status attributes.'
  printfmv('name',5, str(serv_process.name),45)
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

def cmd_showlogint():
  'Display all authenticated users'
  global serv_login_table
  for login_user in serv_login_table.keys():
    printfmv(colorama.Fore.RED + colorama.Style.BRIGHT + login_user + colorama.Fore.WHITE + colorama.Style.NORMAL, 0,'\nPrivilege: {:s}'.format(str(serv_login_table[login_user][1])),0)
    printfmv('Key: ', 0,'{:s}'.format(str(serv_login_table[login_user][0])),50)

def cmd_shutdown():
  'Signal all active servers to shutdown, release resources, and close core server.'
  cmd_loginclose()
  os._exit(0)

def cmd_loginstart():
  'Start the login server with standard configuration.'
  global serv_login, serv_login_commlink, serv_login_comlink_listener, serv_port
  # Support only one login server
  if serv_login != None and serv_login.is_alive():
      printerr('login server has already been started.')
      return
  # Generate new login server
  serv_login_commlink = multiprocessing.Pipe()                              # Establish comlink between core and login
  serv_login = multiprocessing.Process(                                     # Create login server process
    target = login_server.login_server, 
    args = (serv_host, serv_port, serv_login_commlink, serv_login_table), 
    name = lserv_name + str(serv_port)
  )
  serv_login.daemon = True                                                  # Login server exits on core server exit
  serv_login.start()                                                        # Login server process begin
  server_status(serv_login)                                                 # Echo login server status
  serv_port += 1                                                            # Increment port number
  # Generate new login server listener
  serv_login_comlink_listener = multiprocessing.Process(                    # Start tracking login server
    target = logintrack,
    args = (serv_login_commlink[0],),
    name = lserv_name + str(serv_port) + 'ComLink'
  )
  serv_login_comlink_listener.start()

  printlserv('login server operational.')
  printlserv('login server comlink established.')

def logintrack(login_comlink):
  'Login server comlink listener process'
  colorama.init()
  while True:
    try:
      if login_comlink.poll(15):
        message = login_comlink.recv()
        printlserv(message)
    except EOFError:
      printlserv('communication with login server comlink severed.')
      break
    except:
      printlserv('mayday! mayday! mayday!')
      printlserv(sys.exc_info()[1])
      break
  os._exit(0)

def cmd_loginclose():
  'Terminate login server'
  if serv_login != None and serv_login.is_alive():
      printfmv('info',5,'killing %s (%d)' % (serv_login.name, serv_login.pid),45)
      serv_login_commlink[0].close()
      serv_login_commlink[1].close()
      os.kill(serv_login.pid,signal.SIGTERM)              # send login server SIGTERM

cmd_menu_help = {
   '1. showmenu'     :  'list available commands',
   '2. showstatus'   :  'list server process status',
   '3. showlogint'   :  'show global login table',
   '4. loginstart'   :  'start login server process',
   '5. loginclose'   :  'close login server process',
   '6. shutdown'     :  'shutdown all servers'
}

cmd_menu =  {
   'showmenu'       : cmd_showmenu,
   'showstatus'     : cmd_showstat,
   'showlogint'     : cmd_showlogint,
   'loginstart'     : cmd_loginstart,
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

  # setup shared process objects
  serv_login_table = multiprocessing.Manager().dict()
  printfm('info: shared proxy login table', colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)

  # start login server automatically
  printsep(50)
  cmd_menu['loginstart']()
  time.sleep(1)                 # wait 1 seconds for core server to run thread and process

  # display initial command list
  printsep(50)
  cmd_menu['showmenu']()
  printsep(50)

  # core server commands
  while True:
    print(colorama.Fore.WHITE + colorama.Style.BRIGHT + 'comm: ' + colorama.Style.NORMAL, end = '')
    cmd = input()                             # retrieve user command and arguments
    cmd = cmd.split(' ')                      # parse user command
    if cmd[0] in cmd_menu: cmd_menu[cmd[0]]() # execute user command