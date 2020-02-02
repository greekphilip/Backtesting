CREATE TABLE IF NOT EXISTS backtesting.algo (	opentime int8 NULL,	"open" numeric NULL,	high numeric NULL,	low numeric NULL,	"close" numeric NULL,	id int4 NOT NULL,	CONSTRAINT algo_pk PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS backtesting.etc (	opentime int8 NULL,	"open" numeric NULL,	high numeric NULL,	low numeric NULL,	"close" numeric NULL,	id int4 NOT NULL,	CONSTRAINT etc_pk PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS backtesting.nano (	opentime int8 NULL,	"open" numeric NULL,	high numeric NULL,	low numeric NULL,	"close" numeric NULL,	id int4 NOT NULL,	CONSTRAINT nano_pk PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS backtesting.ada (	opentime int8 NULL,	"open" numeric NULL,	high numeric NULL,	low numeric NULL,	"close" numeric NULL,	id int4 NOT NULL,	CONSTRAINT ada_pk PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS backtesting.icx (	opentime int8 NULL,	"open" numeric NULL,	high numeric NULL,	low numeric NULL,	"close" numeric NULL,	id int4 NOT NULL,	CONSTRAINT icx_pk PRIMARY KEY (id));
