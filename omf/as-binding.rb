#!/usr/ruby

def getASList(fileName)
	asList = Hash.new
	File.open(fileName).readlines.each{ |line|
		# Strip newline
		line.chomp!
		cols = line.split(' ')
		asList[cols[0]] = cols[0]
		asList[cols[1]] = cols[1]
	}
	sortedArray = asList.values
	sortedArray.sort!
	return sortedArray
end # getASList

def buildDelayFiles(asList, routeFileName)
	routeFile = File.open(routeFileName)

	asIndex = 0

	# Each line of the matrix
	routeFile.readlines.each { |line|

		# Grab the next AS number
		currAs = asList[asIndex].to_i
		# For each AS number, let's make an output file
		outFile = File.open("as_#{currAs}_delay_client.dat", 'w')
		lineParts = line.gsub(/\s+/m, ' ').strip.split(" ")
		next if lineParts.length == 1

		interlaced = asList.zip(lineParts)

		interlaced.each { |pair|
			asNum = pair[0].to_i
			delay = pair[1]
			if(asNum == currAs)
				outFile.write("192.168.1.#{asNum}, 5001, 5\n")
			else
				outFile.write("192.168.1.#{asNum}, 5001, #{delay}\n")
			end
		}

		outFile.flush
		outFile.close
		asIndex += 1
	}
	routeFile.close
end # buildDelayFiles

asList = getASList ARGV[0]

buildDelayFiles(asList, ARGV[1])
