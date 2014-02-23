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
'''

import os
import sys
import time
import signal
import subprocess
import multiprocessing

import login_server

# server version encoding
appname = 'miniMap'
codename = 'Gefjun'
version = '1.0'
encoding = 'utf-8'
debugmode = True

# server configuration
core_serv_port = 36000
core_serv_host = ''
login_serv_name = 'mini-login'
event_serv_name = 'mini-event'
map_serv_name = 'mini-map'
cur_serv_id = 0

# output formatting function
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
   'Exit core server on ctrl+c.'
   sys.exit()

# core server command handlers
def core_server_status(serv_process):
    printfm('trac: name', str(serv_process.name))
    printfm('trac: pid', str(serv_process.pid))
    printfm('trac: daemon', str(serv_process.daemon))
    printfm('trac: exitcode', str(serv_process.exitcode))
    printfm('trac: alive', str(serv_process.is_alive()))


def core_display_menu():
   'Display all available commands registered in core server menu.'
   printfm('command list', '')
   for menu_item in core_serv_menu_help:
      printfmv(menu_item,15,core_serv_menu_help[menu_item],35)
   printsep(50)

def core_shutdown():
   'Signal all active servers to shutdown, release resources, and close core server.'
   sys.exit()

def core_login():
   'Start the login server with standard configuration.'
   login_serv = multiprocessing.Process(target = None, name = login_serv_name + str(cur_serv_id))
   login_serv.daemon = True
   login_serv.start()
   core_server_status(login_serv)

core_serv_menu_help = {
   'displaymenu'     : 'display available commands',
   'displayserver'   : 'display all active servers',
   'startlogin'      : 'start the login server',
   'shutdown'        : 'shutdown core and active servers'
}

core_serv_menu =  {
   'displaymenu'     : core_display_menu,
   'displayserver'   : None,
   'startlogin'      : core_login,
   'shutdown'        : core_shutdown
}   

# introduction output
printsep(50)
printfm('application', appname)
printfm('server', codename)
printfm('version', version)
printfm('platform', sys.platform)
printfm('encoding', encoding)
printfm('CPU Cores', str(multiprocessing.cpu_count()))
printsep(50)

# set utf-8 encoding on windowas
if sys.platform == 'win32':
   subprocess.call('chcp 65001', shell = True)                             # set console encoding to utf-8
   printfm('info: setting console to utf-8', 'OK')

   subprocess.call('set PYTHONIOENCODING = %s' % encoding, shell = True)   # set python encoding to utf-8
   printfm('info: setting python to utf-8', 'OK')

   printfm('info: system default encoding', sys.getdefaultencoding())      # output current encoding
   printsep(50)

# set asynchronous signals
signal.signal(signal.SIGINT, ctrlc_exit)
printfm('info: registering SIGINT', str(signal.SIGINT) + '/OK')
signal.signal(signal.SIGTERM, ctrlc_exit)
printfm('info: registering SIGTERM', str(signal.SIGTERM) + '/OK')
printsep(50)

# display initial command list
core_serv_menu['displaymenu']()

# core server commands
while True:
   user_cmd = input('cmd >> ')                     # retrieve user command and arguments
   user_cmd_parsed = user_cmd.split(' ')
   if debugmode: print('echo: ', user_cmd_parsed)

   if user_cmd_parsed[0] in core_serv_menu:        # execute user command
      core_serv_menu[user_cmd_parsed[0]]()