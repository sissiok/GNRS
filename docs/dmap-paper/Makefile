FILE = paper

all:
	latex $(FILE)
	bibtex $(FILE)
	latex $(FILE)
	latex $(FILE)
	latex $(FILE)
	make proper
	dvips -Pcmz -Ppdf -tletter $(FILE).dvi -o $(FILE).ps
	ps2pdf ${FILE}.ps
	#rm ${FILE}.ps
	make proper
	#dvipdfm -p letter ${FILE}.dvi

view: all
	xdvi $(FILE).dvi

proper:
	rm -f *.log *.aux *~ *.bbl *.blg *.sav *.bak

spell:
	aspell -c paper.tex
	aspell -c introduction.tex
	aspell -c vlan.tex
	aspell -c config.tex
	aspell -c solution.tex
	aspell -c conclusion.tex

clean: proper
	rm -f $(FILE).pdf $(FILE).dvi

