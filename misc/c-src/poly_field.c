#include	"libc.h"
#include	"fio.h"
#include	"poly.h"
#include	<sys/types.h>
#include	<sys/stat.h>

static char *buf;
static long size;
typedef struct Field
{
	char *field;
	char *start;
} Field;
Field poly_ffields[100];
static nfields;

static
cmp(a, b)
	Field *a, *b;
{
	return(strcmp(a->field, b->field));
}

poly_ifield(s)
	char *s;
{
	int fd;
	struct stat sbuf;

	if((fd = open(s, 0)) < 0){
		perror(s);
		return(1);
	}
	if(fstat(fd, &sbuf) < 0){
		perror(s);
		return(1);
	}
	if(sbuf.st_size >= size){
		if(buf == 0)
			buf = malloc((unsigned)(size = sbuf.st_size+1));
		else
			buf = realloc(buf, (unsigned)(size = sbuf.st_size+1));
	}
	if(read(fd, buf, (int)sbuf.st_size) != sbuf.st_size){
		perror("read");
		return(1);
	}
	close(fd);
	buf[sbuf.st_size] = 0;
	for(s = buf; *s; s++)
		if(*s == '\n') *s = 0;
	for(s = buf, nfields = 0; *s;){
		if(*s == ':'){
			*s++ = 0;
			if(strcmp(s, "EOF") == 0)
				break;
			poly_ffields[nfields].field = s;
			while(*s++);
			poly_ffields[nfields++].start = s;
		}
		while(*s++);
	}
	qsort((char *)poly_ffields, nfields, sizeof(Field), cmp);
	poly_ffields[nfields].field = 0;
	return(0);
}

char *
poly_field(s)
	char *s;
{				/* body duplicated in rfield */
	register l, m, h;

	if(strcmp(s, poly_ffields[0].field) < 0)
		return((char *)0);
	for(l = 0, h = nfields; l != h-1;){
		m = (l+h)/2;
		if(strcmp(s, poly_ffields[m].field) < 0)
			h = m;
		else
			l = m;
	}
	return(strcmp(s, poly_ffields[l].field)? (char *)0 : poly_ffields[l].start);
}

poly_rfield(f, s)
	char *f, *s;
{				/* body duplicated in field */
	register l, m, h;

	if(strcmp(s, poly_ffields[0].field) < 0)
		l = nfields;
	else {
		for(l = 0, h = nfields; l != h-1;){
			m = (l+h)/2;
			if(strcmp(s, poly_ffields[m].field) < 0)
				h = m;
			else
				l = m;
		}
		if(strcmp(s, poly_ffields[l].field))
			l = nfields;
	}
	if(l == nfields){
		nfields++;
		poly_ffields[l].field = f;
	}
	poly_ffields[l].start = s;
}
