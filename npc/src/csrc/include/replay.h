#ifndef __REPLAY_H__
#define __REPLAY_H__


#include <cstdint>
extern void save_model(const char *filenamep);
extern void retore_model(const char *filenamep);

extern void add_chk(uint64_t cycle);
extern void replay(uint64_t wrong_point);

#endif