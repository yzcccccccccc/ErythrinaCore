#ifndef __TRACE_H__
#define __TRACE_H__

#include <cstdio>
extern FILE *itrace_file, *mtrace_file, *irbuf_file;

extern void itrace_init();
extern void itrace_record();
extern void mtrace_init();
extern void mtrace_record();

extern void irbuf_dump();

#endif