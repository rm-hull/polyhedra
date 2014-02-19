#include	"libc.h"
#include	"string.h"
#include	"fio.h"
#include	"poly.h"

static short **facelist();
#define		SPLIT(s)	getmfields(s, ptr, sizeof ptr/sizeof ptr[0])

poly_read(p, dir, n)
	register Polyhedron *p;
	char *dir;
{
	char buf[128];
	char *s, *nexts;
	int i, j, k;
	short *sp;
	char *ptr[10];

	sprint(buf, "%s/%d", dir? dir:POLYDB, n);
	if(poly_ifield(buf))
		return(1);
	p->number = atoi(poly_field("number"));
	p->name = strdup(poly_field("name"));
	p->comment = strdup(poly_field("comment"));
	p->symbol = strdup(poly_field("symbol"));
	p->dual = strdup(poly_field("dual"));
	p->sfaces = strdup(poly_field("sfaces"));
	p->svertices = strdup(poly_field("svertices"));
	s = poly_field("vertices");
	if(p->nvertices = atoi(s)){
		p->vertices = (Vertex *)malloc(p->nvertices*sizeof(Vertex));
		for(nexts = s; *nexts++;);
		if(SPLIT(s) == 2)
			p->nnet = atoi(ptr[1]);
		else
			p->nnet = p->nvertices;
		for(i = 0, s = nexts; *s; s = nexts, i++){
			for(nexts = s; *nexts++;);
			if(SPLIT(s) != 3){
				fprint(2, "bad vertex in file %s (next line is %s)\n", buf, nexts);
				return(1);
			}
			rdvalue(&p->vertices[i].x, ptr[0]);
			rdvalue(&p->vertices[i].y, ptr[1]);
			rdvalue(&p->vertices[i].z, ptr[2]);
		}
		if(i != p->nvertices){
			fprint(2, "%s: expected %d vertices, read %d\n", buf,
				p->nvertices, i);
			return(1);
		}
	}
	p->maxface = 0;
	p->nface = 0;
	p->net = facelist(p, "net", buf);
	p->solid = facelist(p, "solid", buf);
	s = poly_field("hinges");
	if(p->nhinges = atoi(s)){
		p->hinges = (Hinge *)malloc(p->nhinges*sizeof(Hinge));
		while(*s++);
		for(i = 0; *s; s = nexts, i++){
			for(nexts = s; *nexts++;);
			if(SPLIT(s) != 5){
				fprint(2, "%s: bad hinge line\n", buf);
				return(1);
			}
			p->hinges[i].f1 = atoi(ptr[0]);
			p->hinges[i].s1 = atoi(ptr[1]);
			p->hinges[i].f2 = atoi(ptr[2]);
			p->hinges[i].s2 = atoi(ptr[3]);
			rdvalue(&p->hinges[i].d, ptr[4]);
		}
		if(i != p->nhinges){
			fprint(2, "%s: expected %d hinges, read %d\n", buf,
				p->nhinges, i);
			return(1);
		}
	}
	s = poly_field("dihedral");
	if(p->ndih = atoi(s)){
		p->dih = (Dihedral *)malloc(p->ndih*sizeof(Dihedral));
		while(*s++);
		for(i = 0; i < p->ndih; s = nexts, i++){
			for(nexts = s; *nexts++;);
			k = SPLIT(s);
			if(k == 4){
				rdvalue(&p->dih[i].s, ptr[2]);
				rdvalue(&p->dih[i].c, ptr[3]);
				p->dih[i].sc = 1;
				k -= 2;
			} else
				p->dih[i].sc = 0;
			if(k != 2){
				fprint(2, "%s: bad dihedral count\n", buf);
				return(1);
			}
			k = p->dih[i].count = atoi(ptr[0]);
			rdvalue(&p->dih[i].d, ptr[1]);
			s = nexts;
			if(k){
				sp = p->dih[i].fe = (short *)malloc(2*k*sizeof(short));
				for(j = 0; j < k; s = nexts, j++){
					for(nexts = s; *nexts++;);
					if(SPLIT(s) != 2){
						fprint(2, "%s: bad face-edge\n", buf);
						return(1);
					}
					*sp++ = atoi(ptr[0]);
					*sp++ = atoi(ptr[1]);
				}
			} else
				p->dih[i].fe = 0;
		}
		if(i != p->ndih){
			fprint(2, "%s: expected %d dihedrals, read %d\n", buf,
				p->ndih, i);
			return(1);
		}
	}
	s = poly_field("dih");
	if(p->ndgoo = atoi(s)){
		p->dgoo = (Hinge *)malloc(p->ndgoo*sizeof(Hinge));
		while(*s++);
		for(i = 0; *s; s = nexts, i++){
			for(nexts = s; *nexts++;);
			if(SPLIT(s) != 4){
				fprint(2, "%s: bad dgoo line\n", buf);
				return(1);
			}
			p->dgoo[i].f1 = atoi(ptr[0]);
			p->dgoo[i].s1 = atoi(ptr[1]);
			p->dgoo[i].f2 = atoi(ptr[2]);
			rdvalue(&p->dgoo[i].d, ptr[3]);
		}
		if(i != p->ndgoo){
			fprint(2, "%s: expected %d dgoo, read %d\n", buf,
				p->ndgoo, i);
			return(1);
		}
	}
	return(0);
}

rdvalue(v, s)
	Value *v;
	register char *s;
{
	v->v = atof(s);
	while(*s && (*s != '['))
		s++;
	if(*s == '['){
		char *ss = ++s;

		while(*s && (*s != ']'))
			s++;
		*s = 0;
		v->code = strdup(ss);
	} else
		v->code = 0;
}

static short **
facelist(p, ss, file)
	Polyhedron *p;
	char *ss, *file;
{
	char *s, *nexts;
	char *ptr[100];
	int i, j, k, n;
	short **ret;

	s = poly_field(ss);
	if(atoi(s) == 0)
		return((short **)0);
	nexts = strchr(s, 0)+1;
	if(SPLIT(s) != 2){
		fprint(2, "%s: bad count line for '%s'\n", file, ss);
		return((short **)0);
	}
	n = atoi(ptr[0]);
	p->maxface = atoi(ptr[1]);
	if(p->nface){
		if(p->nface != n){
			fprint(2, "%s: nface disagreement: %d vs %d\n", file,
				p->nface, n);
			return((short **)0);
		}
	} else
		p->nface = n;
	ret = (short **)malloc(p->nface*sizeof(short *));
	s = nexts;
	for(i = 0; *s && (i < p->nface); i++, s = nexts){
		nexts = strchr(s, 0)+1;
		SPLIT(s);
		k = atoi(ptr[0]);
		ret[i] = (short *)malloc((k+1)*sizeof(short));
		for(j = 0; j <= k; j++)
			ret[i][j] = atoi(ptr[j]);
	}
	return(ret);
}
