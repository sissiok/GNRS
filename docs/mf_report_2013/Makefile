paper:
	latex paper
	-bibtex --min-crossrefs=20 paper
	latex paper
	latex paper
	dvips -t letter -o paper.ps paper 

pdf: paper
	ps2pdf paper.ps

html:	
	latex2html -nonavigation -math -html_version '4.0' -split 0 paper.tex

clean:
	rm -f *.dvi *.aux *.bbl *.ps *.pdf *.rtf *.blg *.log *~
