ENTRY(_start)

MEMORY
{
    RAM (rwx) : ORIGIN = 0x20000000, LENGTH = 128K
}

SECTIONS
{
    .text :
    {
        *(.text)
    } > RAM

}
