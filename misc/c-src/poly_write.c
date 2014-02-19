#include	"libc.h"
#include	"fio.h"
#include	"poly.h"

typedef struct Field
{
	char *field;
	char *start;
} Field;
extern Field poly_ffields[];

poly_write(p, fd)
	register Polyhedron *p;
{
	int i, j, k;
	short *sp;

	Fprint(fd, ":name\n%s\n", p->name);
	Fprint(fd, ":number\n%d\n", p->number);
	Fprint(fd, ":comment\n%d\n", p->comment);
	if(*p->symbol)
		Fprint(fd, ":symbol\n%s\n", p->symbol);
	if(*p->dual)
		Fprint(fd, ":dual\n%s\n", p->dual);
	if(*p->sfaces)
		Fprint(fd, ":sfaces\n%s\n", p->sfaces);
	if(*p->svertices)
		Fprint(fd, ":svertices\n%s\n", p->svertices);
	if(p->net)
		wrlist(p->net, p->nface, p->maxface, "net", fd);
	if(p->solid)
		wrlist(p->solid, p->nface, p->maxface, "solid", fd);
	if(p->nhinges){
		Fprint(fd, ":hinges\n%d\n", p->nhinges);
		for(i = 0; i < p->nhinges; i++){
			Fprint(fd, "%d %d %d %d ", p->hinges[i].f1,
				p->hinges[i].s1, p->hinges[i].f2, p->hinges[i].s2);
			wrvalue(&p->hinges[i].d, fd);
			Fputc(fd, '\n');
		}
	}
	if(p->ndih){
		Fprint(fd, ":dihedral\n%d\n", p->ndih);
		for(i = 0; i < p->ndih; i++){
			Fprint(fd, "%d ", k = p->dih[i].count);
			wrvalue(&p->dih[i].d, fd);
			if(p->dih[i].sc){
				wrvalue(&p->dih[i].s, fd);
				Fputc(fd, ' ');
				wrvalue(&p->dih[i].c, fd);
			}
			Fputc(fd, '\n');
			if(k){
				sp = p->dih[i].fe;
				for(j = 0; j < k; j++){
					Fprint(fd, "%d %d\n", sp[0], sp[1]);
					sp += 2;
				}
			}
		}
	}
	if(p->ndgoo){
		Fprint(fd, ":dih\n%d\n", p->ndgoo);
		for(i = 0; i < p->ndgoo; i++){
			Fprint(fd, "%d %d %d ", p->dgoo[i].f1, p->dgoo[i].s1,
				p->dgoo[i].f2);
			wrvalue(&p->dgoo[i].d, fd);
			Fputc(fd, '\n');
		}
	}
	if(p->nvertices){
		Fprint(fd, ":vertices\n%d %d\n", p->nvertices, p->nnet);
		for(i = 0; i < p->nvertices; i++){
			wrvalue(&p->vertices[i].x, fd);
			Fputc(fd, ' ');
			wrvalue(&p->vertices[i].y, fd);
			Fputc(fd, ' ');
			wrvalue(&p->vertices[i].z, fd);
			Fputc(fd, '\n');
		}
	}
	Fprint(fd, ":EOF\n");
	Fflush(fd);
	return(0);
}

wrvalue(v, fd)
	Value *v;
{
	Fprint(fd, "%.17g", v->v);
	if(v->code)
		Fprint(fd, "[%s]", v->code);
}

wrlist(list, n, max, str, fd)
	register short **list;
	char *str;
{
	register short *p;
	register i, k;

	Fprint(fd, ":%s\n%d %d\n", str, n, max);
	for(i = 0; i < n; i++){
		p = list[i];
		Fprint(fd, "%d", k = *p++);
		while(k--)
			Fprint(fd, " %d", *p++);
		Fputc(fd, '\n');
	}
}
