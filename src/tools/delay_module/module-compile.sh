#!/bin/bash

LOG=/var/log/delaymodule-boot.log
	
MODULE_DIR=/usr/local/mobilityfirst
MODULE_ELEMENTS_DIR=$MODULE_DIR/code/gnrsd/src/tools/delay_module

CLICK_DIR=/usr/local/src/click

#copy MF click elements into click codebase and compile click
echo -e "\n>>> COPYING MF ROUTER ELEMENTS TO CLICK CODEBASE >>>\n"
echo -e "\n>>> COPYING MF ROUTER ELEMENTS TO CLICK CODEBASE >>>\n" >> $LOG 2>&1
rm $CLICK_DIR/elements/local/*
cp $MODULE_ELEMENTS_DIR/*.cc $MODULE_ELEMENTS_DIR/*.hh $CLICK_DIR/elements/local/

echo ">>> COMPILING CLICK >>>"
echo ">>> COMPILING CLICK >>>" >> $LOG 2>&1
cd $CLICK_DIR
./configure --enable-local >> $LOG 2>&1
CLICK_COMPILE_ERRORS=0

make elemlist >> $LOG 2>&1
if [ $? -ne 0 ] ; then
        CLICK_COMPILE_ERRORS=1
fi

if [ $CLICK_COMPILE_ERRORS -eq 0 ] ; then
	make >> $LOG 2>&1
	if [ $? -ne 0 ] ; then
        	CLICK_COMPILE_ERRORS=1
	fi
fi

if [ $CLICK_COMPILE_ERRORS -eq 0 ] ; then
	make -i install >> $LOG 2>&1
	if [ $? -ne 0 ] ; then
	        CLICK_COMPILE_ERRORS=1
	fi
fi
	
if [ "$CLICK_COMPILE_ERRORS" -eq 1 ] ; then
        echo "There were errors during Click compilation"
fi


echo -e "\n>>> DELAYMODULE BOOT COMPLETE `date` >>>\n"
echo -e "\n>>> DELAYMODULE BOOT COMPLETE `date` >>>\n" >> $LOG 2>&1

echo -e "\nCheck /var/log/delaymodule-boot.log for any errors\n"
