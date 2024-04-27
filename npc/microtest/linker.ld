ENTRY(_start)

MEMORY
{
    RAM     (rwx)   : ORIGIN = 0x20000000, LENGTH = 128K
    FLASH   (rx)    : ORIGIN = 0x30000000, LENGTH = 128K
}

SECTIONS
{
    .text :
    {
        *(.text)
    } > FLASH

}
