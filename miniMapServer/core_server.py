'''
File: core_server.py
Auth: Jim Ching
Desc: administrative server

Documentation:
   packet_specification.txt         :  describe packet design
   core_server_specification.txt    :  describe core server design
   login_server_specification.txt   :  describe login server design
Changelog:
   [1000] - Initial core server backbone.
   [1001] - Clean the code and added colors.
'''

import os                     # OS Servicess
import sys                    # Environment
import signal                 # Asynchronous Signals
import colorama               # Color shell text
import subprocess             # Execute shell commands
import multiprocessing        # Spawn login, event, and map server

# server version encoding
ver_app   = 'miniMap'         # Application Name
ver_proj  = 'Gefjun'          # Project Name
ver_pver  = '1.0'             # Project Version
ver_encd  = 'utf-8'           # Default Encoding

# server configuration
serv_port = 33600             # Server Port [33600 - 33700]
serv_host = ''                # Server Host
lserv_name = 'mini-login'     # Login server process name
eserv_name = 'mini-event'     # Event server process name
mserv_name = 'mini-map'       # Map server process name
serv_id = 1001                # Unique postfix server ID

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

# asynchronous signal handlers
def ctrlc_exit(signal_name, signal_frame): 
   'Exit core server on ctrl + c.'
   sys.exit()

# core server command handlers
def server_status(serv_process):
  'Display server process status attributes.'
  printfm('info: name', str(serv_process.name))
  printfm('info: pid', str(serv_process.pid))
  printfm('info: daemon', str(serv_process.daemon))
  printfm('info: exitcode', str(serv_process.exitcode))
  printfm('info: alive', str(serv_process.is_alive()))

def cmd_showmenu():
  'Display all available commands registered in core server menu.'
  for menu_item in sorted(cmd_menu_help, key = lambda contain: contain[0]):
    printfmv(menu_item,15,cmd_menu_help[menu_item],35)

def cmd_showstat():
  'Display all server processes and status'
  for server_process in multiprocessing.active_childern():
    server_status(server_process)
def cmd_shutdown():
   'Signal all active servers to shutdown, release resources, and close core server.'
   sys.exit()

def cmd_loginstart():
   'Start the login server with standard configuration.'
   login_serv = multiprocessing.Process(target = None, name = lserv_name + str(serv_id))
   login_serv.daemon = True
   login_serv.start()
   server_status(login_serv)

cmd_menu_help = {
   '1.showmenu'     : 'list available commands',
   '2.showstatus'   : 'list server process status',
   '3.startlogin'   : 'start login server',
   '4.shutdown'     : 'shutdown all servers'
}

cmd_menu =  {
   'showmenu'       : cmd_showmenu,
   'showstatus'     : cmd_showstat,
   'loginstart'     : cmd_loginstart,
   'shutdown'       : cmd_shutdown
}   

# Core server must be execute as top-level script
if __name__ == '__main__':
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
     printsep(50)

  # set asynchronous signals
  signal.signal(signal.SIGINT, ctrlc_exit)
  signal.signal(signal.SIGTERM, ctrlc_exit)
  printfm('info: registering SIGINT', str(signal.SIGINT) + '/' + colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)
  printfm('info: registering SIGTERM', str(signal.SIGTERM) + '/' + colorama.Fore.GREEN + 'OK' + colorama.Fore.WHITE)
  printsep(50)

  # display initial command list
  cmd_menu['showmenu']()
  printsep(50)

  # core server commands
  while True:
     cmd = input('cmd >> ')             # retrieve user command and arguments
     cmd = cmd.split(' ')

     if cmd[0] in cmd_menu:        # execute user command
        cmd_menu[cmd[0]]()