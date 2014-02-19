#include	"libc.h"
#include	"fio.h"
#include	"poly.h"

static char names[MAXPOLY][128];
static int ns;

poly_lk(s)
	char *s;
{
	register i;
	register char *ss;
	char buf[128];
	int n = strlen(s);
	static doread = 1;

	for(ss = s; *ss; ss++)
		if((*ss < '0') || (*ss > '9')) break;
	if(*ss == 0)
	{
		n = atoi(s);
		sprint(buf, "%s/%d", POLYDB, n);
		if(access(buf, 4) == 0)
			return(n);
		fprint(2, "number %d out of range\n", n);
		return(-1);
	}
	if(doread){
		readnames();
		doread = 0;
	}
	for(i = 0; i < ns; i++)
		if(strncmp(&names[i][0], s, n) == 0) return(i);
	fprint(2, "no solid '%s'\n", s);
	return(-1);
}

readnames()
{
	register i, fd;
	register char *s;
	char buf[128];

	sprint(buf, "%s/index", POLYDB);
	if((fd = open(buf, 0)) < 0){
		perror(buf);
		exit(1);
	}
	ns = 0;
	while(s = Frdline(fd)){
		i = atoi(s);
		s = strchr(s, '\t');
		strcpy(&names[i][0], s+1);
		if(i >= ns)
			ns = i+1;
	}
	close(fd);
}
