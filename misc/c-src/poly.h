#define	POLYDB		"../poly"
#define	MAXPOLY		4096

typedef struct Value
{
	double v;
	char *code;
} Value;

typedef struct Vertex
{
	Value x, y, z;
} Vertex;

typedef struct Dihedral
{
	Value d;
	short sc;		/* if nonzero s, c are set */
	short count;
	short *fe;		/* f,e * count */
	Value s, c;
} Dihedral;

typedef struct Hinge
{
	short f1, s1, f2, s2;
	Value d;	/* dihedral angle (>PI is reverse fold) */
} Hinge;

typedef struct Polyhedron
{
	short number;
	char *name;
	char *comment;
	char *dual;
	char *symbol;
	char *sfaces;
	char *svertices;
	short nvertices, nnet;
	Vertex *vertices;
	short nface;
	short maxface;		/* max number of sides in a face */
	short **net;
	short **solid;
	short ndih;
	Dihedral *dih;
	short nhinges;
	Hinge *hinges;
	short ndgoo;
	Hinge *dgoo;
} Polyhedron;

extern char *poly_field();
