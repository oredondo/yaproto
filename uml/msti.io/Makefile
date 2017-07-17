# Copyright 2012 Javier Ramirez
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.savarese.com/software/ApacheLicense-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

UNAME  := $(shell uname)
PWD := $(shell pwd)

BINDIR = $(PWD)/bin
SRCDIR = $(PWD)/src
LIBDIR = $(PWD)/lib

# sun-java6-jdk
JDK_BIN="/usr/lib/jvm/java-6-sun-1.6.0.26/bin/"
# gcj
JDK_BIN="/usr/bin/"

JAVA    = $(JDK_BIN)java
JAVAC    = $(JDK_BIN)javac

JFLAGS  = -encoding ISO-8859-1
JARS  := $(shell echo $(LIBDIR)/*.jar | tr ' ' ':')
LIBFLAGS = -cp "$(JARS):$(SRCDIR)"
LDFLAGS = -cp "$(JARS):$(BINDIR)" -Djava.library.path="$(LIBDIR):$(BINDIR)"

JAVA_INCDIR      = $(JDK_HOME)/include
JAVA_INCDIR_PLAF = $(dir $(wildcard $(JAVA_INCDIR)/*/jni_md.h))

SRC := $(shell (find $(SRCDIR) -name "*.java" -print))
BINOBJ := $(shell (find $(SRCDIR) -name "*.java" -print | sed -e "s@^$(SRCDIR)@$(BINDIR)@g"))
OBJ := $(BINOBJ:%.java=%.class)
LIB := $(shell (find $(LIBDIR) -name "*.so" -print))

CLEAN_EXTENSIONS = class o so

all: $(OBJ) $(LIB)

$(BINDIR)/%.class: $(SRCDIR)/%.java
	$(JAVAC) $(JFLAGS) $(LIBFLAGS) -d $(BINDIR)/ -sourcepath $(SRCDIR)  $<  

$(LIB): 
	cd jni && make

clean:
	for extension in $(CLEAN_EXTENSIONS); do \
		find $(BINDIR) -name "*.$$extension" | xargs rm -f ; \
	done
	find . -name "*~" | xargs rm -f
	find . -name "*.bak" | xargs rm -f


RIPMAIN =  msti.rip.ProtocoloRIPv2

rip: all
	ldconfig -l $(LIBDIR)/librocksaw.so
	$(JAVA) $(LDFLAGS) $(RIPMAIN) 
